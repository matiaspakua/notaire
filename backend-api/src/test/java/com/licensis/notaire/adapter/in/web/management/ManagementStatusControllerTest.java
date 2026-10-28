package com.licensis.notaire.adapter.in.web.management;

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

import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase.SaveManagementStatusCommand;
import com.licensis.notaire.application.port.in.management.QueryManagementStatusesUseCase;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementStatusInUseException;
import com.licensis.notaire.domain.management.ManagementStatusInvalidException;
import com.licensis.notaire.domain.management.ManagementStatusNotFoundException;
import com.licensis.notaire.domain.management.ManagementStatusReferencedException;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.management.ManagementStatusStaleVersionException;

/**
 * The controller and its advice over mocked inbound ports (issue #1719). End-to-end REST behaviour is pinned by
 * {@code ManagementStatusApiCharacterizationTest}.
 */
@DisplayName("ManagementStatusController and its exception advice (issue #1719)")
class ManagementStatusControllerTest {

    private final QueryManagementStatusesUseCase query = mock(QueryManagementStatusesUseCase.class);
    private final ManageManagementStatusesUseCase manage = mock(ManageManagementStatusesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new ManagementStatusController(query, manage))
            .setControllerAdvice(new GlobalExceptionHandler(), new ManagementStatusExceptionHandler())
            .build();

    private static final String BODY = "{\"name\": \"Active\", \"notes\": \"n\", \"version\": 3}";

    @Test
    @DisplayName("search maps the matches to the wire shape")
    void searches() throws Exception {
        when(query.searchByName("Act"))
                .thenReturn(List.of(ManagementStatus.restore(1, "Active", "n", 2)));

        mvc.perform(get("/api/v1/management-statuses/search").param("name", "Act"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idManagementStatus").value(1))
                .andExpect(jsonPath("$[0].name").value("Active"))
                .andExpect(jsonPath("$[0].version").value(2));
    }

    @Test
    @DisplayName("in-use returns the answer of the use case")
    void inUse() throws Exception {
        when(query.isInUse(1)).thenReturn(true);
        when(query.isInUse(2)).thenReturn(false);

        mvc.perform(get("/api/v1/management-statuses/1/in-use")).andExpect(jsonPath("$.inUse").value(true));
        mvc.perform(get("/api/v1/management-statuses/2/in-use")).andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("update passes the fields and the client version to the use case")
    void updatePassesNameNotesAndVersion() throws Exception {
        mvc.perform(put("/api/v1/management-statuses/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());

        ArgumentCaptor<SaveManagementStatusCommand> command =
                ArgumentCaptor.forClass(SaveManagementStatusCommand.class);
        verify(manage).update(eq(1), command.capture());
        org.assertj.core.api.Assertions.assertThat(command.getValue())
                .isEqualTo(new SaveManagementStatusCommand("Active", "n", 3));
    }

    @Test
    @DisplayName("not found answers an empty 404")
    void notFound() throws Exception {
        when(query.getById(9)).thenThrow(new ManagementStatusNotFoundException(9));

        mvc.perform(get("/api/v1/management-statuses/9"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("a locked status answers 409 with the error text")
    void locked() throws Exception {
        doThrow(ManagementStatusInUseException.lockedForEdit()).when(manage).update(eq(1), any());

        mvc.perform(put("/api/v1/management-statuses/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ManagementStatusInUseException.LOCKED_MESSAGE));
    }

    @Test
    @DisplayName("deleting a status in use answers 409 with the error text")
    void deleteGuard() throws Exception {
        doThrow(ManagementStatusInUseException.cannotDelete()).when(manage).delete(1);

        mvc.perform(delete("/api/v1/management-statuses/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ManagementStatusInUseException.DELETE_MESSAGE));
    }

    @Test
    @DisplayName("a status the store refuses to delete answers 409 with the error body")
    void referencedFallback() throws Exception {
        doThrow(new ManagementStatusReferencedException(new DataIntegrityViolationException("fk")))
                .when(manage)
                .delete(1);

        mvc.perform(delete("/api/v1/management-statuses/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ManagementStatusInUseException.DELETE_MESSAGE));
    }

    @Test
    @DisplayName("a stale version answers the standard 409")
    void staleVersion() throws Exception {
        doThrow(new ManagementStatusStaleVersionException(1, 4, 3)).when(manage).update(eq(1), any());

        mvc.perform(put("/api/v1/management-statuses/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("The record was modified by another user; reload it and try again"));
    }

    @Test
    @DisplayName("a missing name answers the standard constraint 400")
    void invalid() throws Exception {
        when(manage.create(any())).thenThrow(new ManagementStatusInvalidException("name is required"));

        mvc.perform(post("/api/v1/management-statuses").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: must not be blank"));
    }

    @Test
    @DisplayName("create maps the created status to the wire shape")
    void createMaps() throws Exception {
        when(manage.create(any())).thenReturn(ManagementStatus.restore(5, "Active", "n", 0));

        mvc.perform(post("/api/v1/management-statuses").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idManagementStatus").value(5))
                .andExpect(jsonPath("$.name").value("Active"))
                .andExpect(jsonPath("$.version").value(0));
    }
}
