package com.licensis.notaire.adapter.in.web.folio;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.application.port.in.folio.ManageFoliosUseCase;
import com.licensis.notaire.application.port.in.folio.QueryFoliosUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.folio.FolioInUseException;
import com.licensis.notaire.domain.folio.FolioNotFoundException;

@DisplayName("FolioExceptionHandler maps domain exceptions (#577)")
class FolioExceptionHandlerTest {

    private final ManageFoliosUseCase manage = mock(ManageFoliosUseCase.class);
    private final MockMvc mvc = standaloneSetup(new FolioController(mock(QueryFoliosUseCase.class), manage))
            .setControllerAdvice(new FolioExceptionHandler(), new GlobalExceptionHandler())
            .build();

    private static final String BODY =
            "{\"number\": 1, \"year\": 2026, \"status\": \"Disponible\", \"typeFolioId\": 1, \"notaryId\": 1}";

    @Test
    @DisplayName("update FolioInUse returns 409 error body")
    void updateInUseReturnsConflict() throws Exception {
        when(manage.update(eq(1), any())).thenThrow(FolioInUseException.cannotModify());

        mvc.perform(put("/api/v1/folios/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("This folio is in use (Utilizado) and cannot be modified."));
    }

    @Test
    @DisplayName("delete FolioNotFound returns 404")
    void deleteNotFoundReturns404() throws Exception {
        doThrow(new FolioNotFoundException(9)).when(manage).delete(9);

        mvc.perform(delete("/api/v1/folios/9")).andExpect(status().isNotFound());
    }
}
