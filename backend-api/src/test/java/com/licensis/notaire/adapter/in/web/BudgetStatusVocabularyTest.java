package com.licensis.notaire.adapter.in.web;

import com.licensis.notaire.adapter.in.web.budget.BudgetController;
import com.licensis.notaire.application.usecase.budget.BudgetService;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.BudgetStatus;
import com.licensis.notaire.config.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Issue #1346 (Owner default 2026-10-10: the stored vocabulary): a budget status is one of
 * BORRADOR, PENDIENTE, APROBADO, RECHAZADO, FACTURADO. Writes accept any letter case and store
 * the canonical code; anything else answers 400 naming the accepted values. The status search
 * matches whatever case the caller sends.
 */
@DisplayName("Budget status vocabulary (issue #1346)")
class BudgetStatusVocabularyTest {

    private final BudgetService service = mock(BudgetService.class);
    private final MockMvc mvc = build();

    private MockMvc build() {
        try {
            Constructor<?> constructor = BudgetController.class.getConstructors()[0];
            Object[] args = new Object[constructor.getParameterCount()];
            for (int i = 0; i < args.length; i++) {
                Class<?> parameter = constructor.getParameterTypes()[i];
                args[i] = parameter == BudgetService.class ? service : mock(parameter);
            }
            return standaloneSetup(constructor.newInstance(args))
                    .setControllerAdvice(new GlobalExceptionHandler()).build();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String body(String status) {
        return "{\"number\":1,\"date\":\"2026-10-10\",\"encabezado\":\"E\",\"status\":\"" + status + "\"}";
    }

    @Test
    @DisplayName("the vocabulary is the five stored codes, in workflow order")
    void vocabulary() {
        assertThat(BudgetStatus.values()).extracting(Enum::name)
                .containsExactly("BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "FACTURADO");
    }

    @Test
    @DisplayName("POST stores the canonical code whatever the letter case")
    void createNormalizesCase() throws Exception {
        when(service.create(any())).thenAnswer(inv -> inv.getArgument(0));
        mvc.perform(post("/api/v1/presupuestos").contentType(MediaType.APPLICATION_JSON).content(body("Pendiente")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDIENTE"));
    }

    @Test
    @DisplayName("POST with an unknown status answers 400 naming the accepted values and saves nothing")
    void createRejectsUnknown() throws Exception {
        mvc.perform(post("/api/v1/presupuestos").contentType(MediaType.APPLICATION_JSON).content(body("Pending")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "status: valor no válido 'Pending'; valores aceptados: BORRADOR, PENDIENTE, APROBADO, RECHAZADO, FACTURADO"));
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("PUT stores the canonical code and rejects an unknown one")
    void updateNormalizesAndRejects() throws Exception {
        Budget existing = new Budget();
        existing.setIdBudget(5);
        existing.setStatus("BORRADOR");
        when(service.findById(5)).thenReturn(Optional.of(existing));
        when(service.update(eq(5), any())).thenAnswer(inv -> inv.getArgument(1));

        mvc.perform(put("/api/v1/presupuestos/5").contentType(MediaType.APPLICATION_JSON).content(body(" aprobado ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROBADO"));
        mvc.perform(put("/api/v1/presupuestos/5").contentType(MediaType.APPLICATION_JSON).content(body("ACTIVO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /buscar?status= searches the canonical code")
    void searchNormalizes() throws Exception {
        when(service.findByStatus("PENDIENTE")).thenReturn(List.of());
        mvc.perform(get("/api/v1/presupuestos/buscar").param("status", "pendiente"))
                .andExpect(status().isOk());
        verify(service).findByStatus("PENDIENTE");
    }
}
