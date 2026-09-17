package com.licensis.notaire.adapter.in.web.payment;

import com.licensis.notaire.application.port.in.payment.DeletePaymentUseCase;
import com.licensis.notaire.application.port.in.payment.EditPaymentCommand;
import com.licensis.notaire.application.port.in.payment.EditPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.domain.payment.PaymentStatus;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Characterization tests for the payment inbound web adapter: same URLs, same status
 * codes and same JSON as before the hexagonal refactor (ADR-021), now driven through the
 * inbound ports instead of the legacy {@code PaymentService}.
 */
@DisplayName("PagoController unit tests")
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private ProcessPaymentUseCase processPaymentUseCase;

    @Mock
    private EditPaymentUseCase editPaymentUseCase;

    @Mock
    private DeletePaymentUseCase deletePaymentUseCase;

    @Mock
    private QueryPaymentsUseCase queryPaymentsUseCase;

    @Mock
    private GetPaymentStatusUseCase paymentStatusUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentController controller = new PaymentController(processPaymentUseCase, editPaymentUseCase,
                deletePaymentUseCase, queryPaymentsUseCase, paymentStatusUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private PaymentDetails buildPayment() {
        return new PaymentDetails(1, 10, 500.0f, new Date(), null, "Pago de prueba");
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    @DisplayName("GET /api/v1/pagos should return 200 with all pagos")
    void shouldGetAllPayments() throws Exception {
        when(queryPaymentsUseCase.findAll()).thenReturn(List.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPayment").value(1))
                .andExpect(jsonPath("$[0].amount").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos should return 500 on service error")
    void shouldReturnServerErrorWhenGetAllFails() throws Exception {
        when(queryPaymentsUseCase.findAll()).thenThrow(new RuntimeException("Database error"));
        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 200 when pago found")
    void shouldGetPaymentById() throws Exception {
        when(queryPaymentsUseCase.findById(1)).thenReturn(Optional.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPayment").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        when(queryPaymentsUseCase.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/pagos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should include the associated presupuesto")
    void shouldIncludeBudgetWhenRetrievingPaymentById() throws Exception {
        when(queryPaymentsUseCase.findById(1)).thenReturn(Optional.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBudget").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 500 on service error")
    void shouldReturnServerErrorOnGetById() throws Exception {
        when(queryPaymentsUseCase.findById(anyInt())).thenThrow(new RuntimeException("Service error"));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should return pagos for presupuesto")
    void shouldGetPaymentsByBudget() throws Exception {
        when(queryPaymentsUseCase.findByBudget(10)).thenReturn(List.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should include the presupuesto on each entry")
    void shouldIncludeBudgetOnEachListedPayment() throws Exception {
        when(queryPaymentsUseCase.findByBudget(10)).thenReturn(List.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idBudget").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should return 500 on error")
    void shouldReturnServerErrorOnBudgetQuery() throws Exception {
        when(queryPaymentsUseCase.findByBudget(anyInt())).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return saldo pendiente")
    void shouldGetSaldoPending() throws Exception {
        when(paymentStatusUseCase.pendingBalance(10)).thenReturn(1000.0f);
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/saldo"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return 404 for invalid presupuesto")
    void shouldReturn404ForInvalidBudget() throws Exception {
        when(paymentStatusUseCase.pendingBalance(999)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/999/saldo"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return 500 on error")
    void shouldReturnServerErrorOnSaldoCalculation() throws Exception {
        when(paymentStatusUseCase.pendingBalance(anyInt())).thenThrow(new RuntimeException("Calculation error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/saldo"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/estado should return estado de pago")
    void shouldGetStatusPayment() throws Exception {
        when(paymentStatusUseCase.status(10)).thenReturn(PaymentStatus.PARTIAL);
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/estado"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"PARTIAL\""));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/estado should return 404 for invalid presupuesto")
    void shouldReturn404ForStatusOfInvalidBudget() throws Exception {
        when(paymentStatusUseCase.status(999)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/999/estado"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/estado should return 500 on error")
    void shouldReturnServerErrorOnStatusCalculation() throws Exception {
        when(paymentStatusUseCase.status(anyInt())).thenThrow(new RuntimeException("Calculation error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/estado"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/fecha should return pagos in fecha range")
    void shouldGetPaymentsByDateRange() throws Exception {
        when(queryPaymentsUseCase.findByDateRange(any(Date.class), any(Date.class)))
                .thenReturn(List.of(buildPayment()));

        mockMvc.perform(get("/api/v1/pagos/fecha")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(500.0));

        verify(queryPaymentsUseCase).findByDateRange(
                toDate(LocalDate.of(2026, 1, 1)), toDate(LocalDate.of(2026, 12, 31)));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/fecha should return 500 on service error")
    void shouldReturnServerErrorOnDateRangeQuery() throws Exception {
        when(queryPaymentsUseCase.findByDateRange(any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Query error"));

        mockMvc.perform(get("/api/v1/pagos/fecha")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 201 when pago created")
    void shouldCreatePaymentViaJson() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class))).thenReturn(buildPayment());

        String json = """
                {
                    "idBudget": 10,
                    "amount": 500.0,
                    "date": "2026-06-16",
                    "notes": "Payment de prueba"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPayment").value(1));

        ArgumentCaptor<ProcessPaymentCommand> command = ArgumentCaptor.forClass(ProcessPaymentCommand.class);
        verify(processPaymentUseCase, times(1)).process(command.capture());
        assertThat(command.getValue().budgetId()).isEqualTo(10);
        assertThat(command.getValue().amount()).isEqualTo(500.0f);
        assertThat(command.getValue().date()).isNotNull();
        assertThat(command.getValue().notes()).isEqualTo("Payment de prueba");
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return the associated presupuesto when creating a payment")
    void shouldReturnBudgetWhenCreatingPayment() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class))).thenReturn(buildPayment());

        String json = """
                {
                    "idBudget": 10,
                    "amount": 500.0,
                    "date": "2026-06-16",
                    "notes": "Payment de prueba"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idBudget").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 400 on validation error")
    void shouldReturn400OnCreateValidationError() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .thenThrow(new IllegalArgumentException("Invalid amount"));

        String json = """
                {
                    "idBudget": 10,
                    "amount": -100.0,
                    "date": "2026-06-16",
                    "notes": "Invalid"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 409 when monto exceeds saldo pendiente")
    void shouldReturn409WhenCreateExceedsSaldo() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .thenThrow(new PendingBalanceExceededException("no puede exceder el saldo pendiente"));

        String json = """
                {
                    "idBudget": 10,
                    "amount": 999999.0,
                    "date": "2026-06-16",
                    "notes": "Overpay"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 500 on service error")
    void shouldReturn500OnCreateServiceError() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .thenThrow(new RuntimeException("DB error"));

        String json = """
                {
                    "idBudget": 10,
                    "amount": 500.0,
                    "date": "2026-06-16",
                    "notes": "Test"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should return 201 when pago created via params")
    void shouldCreatePaymentViaParams() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class))).thenReturn(buildPayment());

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idBudget", "10")
                .param("amount", "500.0")
                .param("date", "2026-06-16")
                .param("notes", "Test pago"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPayment").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should handle missing optional params")
    void shouldHandleMissingOptionalParams() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class))).thenReturn(buildPayment());

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idBudget", "10")
                .param("amount", "500.0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPayment").value(1));

        ArgumentCaptor<ProcessPaymentCommand> command = ArgumentCaptor.forClass(ProcessPaymentCommand.class);
        verify(processPaymentUseCase).process(command.capture());
        assertThat(command.getValue().budgetId()).isEqualTo(10);
        assertThat(command.getValue().amount()).isEqualTo(500.0f);
        assertThat(command.getValue().date()).isNull();
        assertThat(command.getValue().notes()).isNull();
        assertThat(command.getValue().paymentMethod()).isNull();
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should return 409 when monto exceeds saldo pendiente")
    void shouldReturn409OnParamsExceedsSaldo() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .thenThrow(new PendingBalanceExceededException("no puede exceder el saldo pendiente"));

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idBudget", "10")
                .param("amount", "999999.0"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should return 500 on service error")
    void shouldReturn500OnParamsServiceError() throws Exception {
        when(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idBudget", "10")
                .param("amount", "500.0"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("PUT /api/v1/pagos/{id} should return 200 when updated")
    void shouldUpdatePayment() throws Exception {
        when(editPaymentUseCase.edit(any(EditPaymentCommand.class))).thenReturn(buildPayment());

        String json = """
                {
                    "amount": 600.0,
                    "date": "2026-06-16",
                    "notes": "Updated"
                }
                """;

        mockMvc.perform(put("/api/v1/pagos/1")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPayment").value(1));

        ArgumentCaptor<EditPaymentCommand> command = ArgumentCaptor.forClass(EditPaymentCommand.class);
        verify(editPaymentUseCase).edit(command.capture());
        assertThat(command.getValue().paymentId()).isEqualTo(1);
        assertThat(command.getValue().amount()).isEqualTo(600.0f);
        assertThat(command.getValue().date()).isNotNull();
        assertThat(command.getValue().notes()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("PUT /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404OnUpdateNotFound() throws Exception {
        when(editPaymentUseCase.edit(any(EditPaymentCommand.class)))
                .thenThrow(new IllegalArgumentException("Pago no encontrado"));

        String json = """
                {
                    "amount": 600.0,
                    "date": "2026-06-16",
                    "notes": "Updated"
                }
                """;

        mockMvc.perform(put("/api/v1/pagos/999")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/pagos/{id} should return 500 on service error")
    void shouldReturn500OnUpdateError() throws Exception {
        when(editPaymentUseCase.edit(any(EditPaymentCommand.class)))
                .thenThrow(new RuntimeException("DB error"));

        String json = """
                {
                    "amount": 600.0,
                    "date": "2026-06-16",
                    "notes": "Updated"
                }
                """;

        mockMvc.perform(put("/api/v1/pagos/1")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 200 when deleted")
    void shouldDeletePayment() throws Exception {
        doNothing().when(deletePaymentUseCase).delete(1);
        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isOk());
        verify(deletePaymentUseCase, times(1)).delete(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404OnDeleteNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Pago no encontrado")).when(deletePaymentUseCase).delete(999);
        mockMvc.perform(delete("/api/v1/pagos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 500 on service error")
    void shouldReturn500OnDeleteError() throws Exception {
        doThrow(new RuntimeException("DB error")).when(deletePaymentUseCase).delete(anyInt());
        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isInternalServerError());
    }
}
