package com.licensis.notaire.unit;

import com.licensis.notaire.api.PaymentController;
import com.licensis.notaire.exception.SaldoPendingExcedidoException;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.service.StatusPayment;
import com.licensis.notaire.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.eq;

@DisplayName("PagoController unit tests")
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentController controller = new PaymentController(paymentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Payment buildPayment() {
        Payment payment = new Payment();
        payment.setIdPayment(1);
        payment.setAmount(500.0f);
        payment.setDate(new Date());
        payment.setNotes("Pago de prueba");
        Budget budget = new Budget();
        budget.setIdBudget(10);
        payment.setBudget(budget);
        return payment;
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    @DisplayName("GET /api/v1/pagos should return 200 with all pagos")
    void shouldGetAllPayments() throws Exception {
        when(paymentService.findAll()).thenReturn(List.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPayment").value(1))
                .andExpect(jsonPath("$[0].amount").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos should return 500 on service error")
    void shouldReturnServerErrorWhenGetAllFails() throws Exception {
        when(paymentService.findAll()).thenThrow(new RuntimeException("Database error"));
        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 200 when pago found")
    void shouldGetPaymentById() throws Exception {
        when(paymentService.consultarPayment(1)).thenReturn(Optional.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPayment").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        when(paymentService.consultarPayment(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/pagos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should include the associated presupuesto")
    void shouldIncludeBudgetWhenRetrievingPaymentById() throws Exception {
        when(paymentService.consultarPayment(1)).thenReturn(Optional.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBudget").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 500 on service error")
    void shouldReturnServerErrorOnGetById() throws Exception {
        when(paymentService.consultarPayment(anyInt())).thenThrow(new RuntimeException("Service error"));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should return pagos for presupuesto")
    void shouldGetPaymentsByBudget() throws Exception {
        when(paymentService.findPaymentsByBudget(10)).thenReturn(List.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should include the presupuesto on each entry")
    void shouldIncludeBudgetOnEachListedPayment() throws Exception {
        when(paymentService.findPaymentsByBudget(10)).thenReturn(List.of(buildPayment()));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idBudget").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should return 500 on error")
    void shouldReturnServerErrorOnBudgetQuery() throws Exception {
        when(paymentService.findPaymentsByBudget(anyInt())).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return saldo pendiente")
    void shouldGetSaldoPending() throws Exception {
        when(paymentService.calcularSaldoPending(10)).thenReturn(1000.0f);
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/saldo"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return 404 for invalid presupuesto")
    void shouldReturn404ForInvalidBudget() throws Exception {
        when(paymentService.calcularSaldoPending(999)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/999/saldo"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return 500 on error")
    void shouldReturnServerErrorOnSaldoCalculation() throws Exception {
        when(paymentService.calcularSaldoPending(anyInt())).thenThrow(new RuntimeException("Calculation error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/saldo"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/estado should return estado de pago")
    void shouldGetStatusPayment() throws Exception {
        when(paymentService.calcularStatusPayment(10)).thenReturn(StatusPayment.PARCIAL);
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/estado"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"PARCIAL\""));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/estado should return 404 for invalid presupuesto")
    void shouldReturn404ForStatusOfInvalidBudget() throws Exception {
        when(paymentService.calcularStatusPayment(999)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/999/estado"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/estado should return 500 on error")
    void shouldReturnServerErrorOnStatusCalculation() throws Exception {
        when(paymentService.calcularStatusPayment(anyInt())).thenThrow(new RuntimeException("Calculation error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/estado"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/fecha should return pagos in fecha range")
    void shouldGetPaymentsByDateRange() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        when(paymentService.findPaymentsByDateRange(any(Date.class), any(Date.class)))
                .thenReturn(List.of(buildPayment()));

        mockMvc.perform(get("/api/v1/pagos/fecha")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/fecha should return 500 on service error")
    void shouldReturnServerErrorOnDateRangeQuery() throws Exception {
        when(paymentService.findPaymentsByDateRange(any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Query error"));

        mockMvc.perform(get("/api/v1/pagos/fecha")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 201 when pago created")
    void shouldCreatePaymentViaJson() throws Exception {
        Payment newPayment = buildPayment();
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(Date.class), anyString(), any()))
                .thenReturn(newPayment);

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

        verify(paymentService, times(1)).procesarPayment(eq(10), eq(500.0f), any(Date.class), eq("Payment de prueba"), any());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return the associated presupuesto when creating a payment")
    void shouldReturnBudgetWhenCreatingPayment() throws Exception {
        Payment newPayment = buildPayment();
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(Date.class), anyString(), any()))
                .thenReturn(newPayment);

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
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(), anyString(), any()))
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
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenThrow(new SaldoPendingExcedidoException("no puede exceder el saldo pendiente"));

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
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(), anyString(), any()))
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
        Payment newPayment = buildPayment();
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenReturn(newPayment);

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
        Payment newPayment = buildPayment();
        when(paymentService.procesarPayment(eq(10), eq(500.0f), isNull(), isNull(), isNull()))
                .thenReturn(newPayment);

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idBudget", "10")
                .param("amount", "500.0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPayment").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should return 409 when monto exceeds saldo pendiente")
    void shouldReturn409OnParamsExceedsSaldo() throws Exception {
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(), any(), any()))
                .thenThrow(new SaldoPendingExcedidoException("no puede exceder el saldo pendiente"));

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idBudget", "10")
                .param("amount", "999999.0"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should return 500 on service error")
    void shouldReturn500OnParamsServiceError() throws Exception {
        when(paymentService.procesarPayment(anyInt(), anyFloat(), any(), any(), any()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idBudget", "10")
                .param("amount", "500.0"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("PUT /api/v1/pagos/{id} should return 200 when updated")
    void shouldUpdatePayment() throws Exception {
        Payment updated = buildPayment();
        when(paymentService.editarPayment(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenReturn(updated);

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
    }

    @Test
    @DisplayName("PUT /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404OnUpdateNotFound() throws Exception {
        when(paymentService.editarPayment(anyInt(), anyFloat(), any(), anyString(), any()))
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
        when(paymentService.editarPayment(anyInt(), anyFloat(), any(), anyString(), any()))
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
        doNothing().when(paymentService).deletePayment(1);
        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isOk());
        verify(paymentService, times(1)).deletePayment(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404OnDeleteNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Pago no encontrado")).when(paymentService).deletePayment(999);
        mockMvc.perform(delete("/api/v1/pagos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 500 on service error")
    void shouldReturn500OnDeleteError() throws Exception {
        doThrow(new RuntimeException("DB error")).when(paymentService).deletePayment(anyInt());
        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isInternalServerError());
    }
}
