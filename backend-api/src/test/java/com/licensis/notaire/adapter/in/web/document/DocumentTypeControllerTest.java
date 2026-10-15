package com.licensis.notaire.adapter.in.web.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase;
import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase.SaveDocumentTypeCommand;
import com.licensis.notaire.application.port.in.document.QueryDocumentTypesUseCase;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.DocumentTypeInUseException;
import com.licensis.notaire.domain.document.DocumentTypeInvalidException;
import com.licensis.notaire.domain.document.DocumentTypeNotFoundException;
import com.licensis.notaire.domain.document.DocumentTypeReferencedException;
import com.licensis.notaire.domain.document.DocumentTypeStaleVersionException;

/**
 * The controller and its advice over mocked inbound ports (issue #1705). End-to-end REST behaviour is pinned by
 * {@code DocumentTypeApiCharacterizationTest}.
 */
@DisplayName("DocumentTypeController and its exception advice (issue #1705)")
class DocumentTypeControllerTest {

    private final QueryDocumentTypesUseCase query = mock(QueryDocumentTypesUseCase.class);
    private final ManageDocumentTypesUseCase manage = mock(ManageDocumentTypesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new DocumentTypeController(query, manage))
            .setControllerAdvice(new DocumentTypeExceptionHandler()).build();

    private static final String BODY =
            "{\"name\": \"ID card\", \"expires\": true, \"dueDays\": 30, \"deliveredBy\": \"CLIENT\", "
                    + "\"enabled\": true, \"returned\": false, \"version\": 3}";

    @Test
    @DisplayName("search maps the matches to the wire shape")
    void searches() throws Exception {
        when(query.searchByName("ID")).thenReturn(
                List.of(DocumentType.restore(1, "ID card", true, 30, "CLIENT", true, false, 2)));

        mvc.perform(get("/api/v1/document-types/search").param("name", "ID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDocumentType").value(1))
                .andExpect(jsonPath("$[0].name").value("ID card"))
                .andExpect(jsonPath("$[0].version").value(2));
    }

    @Test
    @DisplayName("in-use returns the answer of the use case")
    void inUse() throws Exception {
        when(query.isInUse(1)).thenReturn(true);
        when(query.isInUse(2)).thenReturn(false);

        mvc.perform(get("/api/v1/document-types/1/in-use")).andExpect(jsonPath("$.inUse").value(true));
        mvc.perform(get("/api/v1/document-types/2/in-use")).andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("update passes the fields and the client version to the use case")
    void updatePassesTheFlagsAndVersion() throws Exception {
        when(query.getById(1)).thenReturn(DocumentType.restore(1, "Old", false, null, "", true, false, 3));

        mvc.perform(put("/api/v1/document-types/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());

        ArgumentCaptor<SaveDocumentTypeCommand> command = ArgumentCaptor.forClass(SaveDocumentTypeCommand.class);
        verify(manage).update(eq(1), command.capture());
        org.assertj.core.api.Assertions.assertThat(command.getValue())
                .isEqualTo(new SaveDocumentTypeCommand("ID card", true, 30, "CLIENT", true, false, 3));
    }

    @Test
    @DisplayName("not found answers an empty 404")
    void notFound() throws Exception {
        when(query.getById(9)).thenThrow(new DocumentTypeNotFoundException(9));

        mvc.perform(get("/api/v1/document-types/9")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("a locked type answers 409 with the error text")
    void locked() throws Exception {
        when(query.getById(1)).thenReturn(DocumentType.restore(1, "ID", false, null, "", true, false, 0));
        doThrow(DocumentTypeInUseException.lockedForEdit()).when(manage).update(eq(1), any());

        mvc.perform(put("/api/v1/document-types/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("This document type is in use and cannot be modified. Create a new one."));
    }

    @Test
    @DisplayName("deleting a type in use answers 409 with the error text")
    void deleteGuard() throws Exception {
        doThrow(DocumentTypeInUseException.cannotDelete()).when(manage).delete(1);

        mvc.perform(delete("/api/v1/document-types/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("Cannot delete: the document type is used by procedure templates or submitted documents."));
    }

    @Test
    @DisplayName("a type the store refuses to delete answers 409 with the error body")
    void referencedFallback() throws Exception {
        doThrow(new DocumentTypeReferencedException(new DataIntegrityViolationException("fk"))).when(manage).delete(1);

        mvc.perform(delete("/api/v1/document-types/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("Cannot delete: the document type is used by procedure templates or submitted documents."));
    }

    @Test
    @DisplayName("a stale version answers the standard 409")
    void staleVersion() throws Exception {
        when(query.getById(1)).thenReturn(DocumentType.restore(1, "ID", false, null, "", true, false, 4));
        doThrow(new DocumentTypeStaleVersionException(1, 4, 3)).when(manage).update(eq(1), any());

        mvc.perform(put("/api/v1/document-types/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("The record was modified by another user; reload it and try again"));
    }

    @Test
    @DisplayName("a missing name answers the standard constraint 400")
    void invalid() throws Exception {
        when(manage.create(any())).thenThrow(new DocumentTypeInvalidException("name is required"));

        mvc.perform(post("/api/v1/document-types").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The submitted data violates a database constraint"));
    }
}
