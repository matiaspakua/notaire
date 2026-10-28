package com.licensis.notaire.adapter.in.web.folio;

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

import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase;
import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase.SaveFolioTypeCommand;
import com.licensis.notaire.application.port.in.folio.QueryFolioTypesUseCase;
import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeInUseException;
import com.licensis.notaire.domain.folio.FolioTypeInvalidException;
import com.licensis.notaire.domain.folio.FolioTypeNotFoundException;
import com.licensis.notaire.domain.folio.FolioTypeReferencedException;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.folio.FolioTypeStaleVersionException;

/**
 * The controller and its advice over mocked inbound ports (issue #1703): what the HTTP layer adds on top of the use
 * cases, namely the wire mapping and each domain exception's response. The REST behaviour end to end is pinned by
 * {@code FolioTypeApiCharacterizationTest}.
 */
@DisplayName("FolioTypeController and its exception advice (issue #1703)")
class FolioTypeControllerTest {

    private final QueryFolioTypesUseCase query = mock(QueryFolioTypesUseCase.class);
    private final ManageFolioTypesUseCase manage = mock(ManageFolioTypesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new FolioTypeController(query, manage))
            .setControllerAdvice(new GlobalExceptionHandler(), new FolioTypeExceptionHandler())
            .build();

    private static final String BODY =
            "{\"name\": \"Protocol\", \"notes\": \"n\", \"enabled\": true, \"isAuxiliary\": true, \"version\": 3}";

    @Test
    @DisplayName("CU68 search maps the matches to the wire shape")
    void searches() throws Exception {
        when(query.searchByName("Proto")).thenReturn(List.of(FolioType.restore(1, "Protocol", "n", true, false, 2)));

        mvc.perform(get("/api/v1/folio-types/search").param("name", "Proto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idFolioType").value(1))
                .andExpect(jsonPath("$[0].name").value("Protocol"))
                .andExpect(jsonPath("$[0].version").value(2));
    }

    @Test
    @DisplayName("in-use returns the answer of the use case")
    void inUse() throws Exception {
        when(query.isInUse(1)).thenReturn(true);
        when(query.isInUse(2)).thenReturn(false);

        mvc.perform(get("/api/v1/folio-types/1/in-use")).andExpect(jsonPath("$.inUse").value(true));
        mvc.perform(get("/api/v1/folio-types/2/in-use")).andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("CU81 update passes the auxiliary flag and the client version to the use case")
    void updatePassesTheFlagsAndVersion() throws Exception {
        mvc.perform(put("/api/v1/folio-types/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());

        ArgumentCaptor<SaveFolioTypeCommand> command = ArgumentCaptor.forClass(SaveFolioTypeCommand.class);
        verify(manage).update(eq(1), command.capture());
        org.assertj.core.api.Assertions.assertThat(command.getValue())
                .isEqualTo(new SaveFolioTypeCommand("Protocol", "n", true, true, 3));
    }

    @Test
    @DisplayName("a missing version is sent as 0, as the DTO default always did")
    void missingVersionIsZero() throws Exception {
        mvc.perform(put("/api/v1/folio-types/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"X\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<SaveFolioTypeCommand> command = ArgumentCaptor.forClass(SaveFolioTypeCommand.class);
        verify(manage).update(eq(1), command.capture());
        org.assertj.core.api.Assertions.assertThat(command.getValue().version()).isZero();
    }

    @Test
    @DisplayName("not found answers an empty 404")
    void notFound() throws Exception {
        when(query.getById(9)).thenThrow(new FolioTypeNotFoundException(9));

        mvc.perform(get("/api/v1/folio-types/9")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("a locked type answers 409 with the error text")
    void locked() throws Exception {
        doThrow(FolioTypeInUseException.lockedForEdit()).when(manage).update(eq(1), any());

        mvc.perform(put("/api/v1/folio-types/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("This folio type is in use and cannot be modified. Create a new one."));
    }

    @Test
    @DisplayName("deleting a type used by folios answers 409 with the error text")
    void deleteGuard() throws Exception {
        doThrow(FolioTypeInUseException.cannotDelete()).when(manage).delete(1);

        mvc.perform(delete("/api/v1/folio-types/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete: the folio type is in use by folios."));
    }

    @Test
    @DisplayName("a type the store refuses to delete answers 409 with the plain text, as before")
    void referencedFallback() throws Exception {
        doThrow(new FolioTypeReferencedException(new DataIntegrityViolationException("fk"))).when(manage).delete(1);

        mvc.perform(delete("/api/v1/folio-types/1"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cannot delete: the folio type is referenced by other records."));
    }

    @Test
    @DisplayName("a stale version answers the standard 409")
    void staleVersion() throws Exception {
        doThrow(new FolioTypeStaleVersionException(1, 4, 3)).when(manage).update(eq(1), any());

        mvc.perform(put("/api/v1/folio-types/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The record was modified by another user; reload it and try again"));
    }

    @Test
    @DisplayName("a missing name answers the standard constraint 400")
    void invalid() throws Exception {
        when(manage.create(any())).thenThrow(new FolioTypeInvalidException("name is required"));

        mvc.perform(post("/api/v1/folio-types").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: must not be blank"));
    }
}
