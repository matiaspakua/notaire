package com.licensis.notaire.unit;

import com.licensis.notaire.api.PagoController;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.service.PagoService;
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
class PagoControllerTest {

    @Mock
    private PagoService pagoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PagoController controller = new PagoController(pagoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Pago buildPago() {
        Pago pago = new Pago();
        pago.setIdPago(1);
        pago.setMonto(500.0f);
        pago.setFecha(new Date());
        pago.setObservaciones("Pago de prueba");
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(10);
        pago.setPresupuesto(presupuesto);
        return pago;
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    @DisplayName("GET /api/v1/pagos should return 200 with all pagos")
    void shouldGetAllPagos() throws Exception {
        when(pagoService.findAll()).thenReturn(List.of(buildPago()));
        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPago").value(1))
                .andExpect(jsonPath("$[0].monto").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos should return 500 on service error")
    void shouldReturnServerErrorWhenGetAllFails() throws Exception {
        when(pagoService.findAll()).thenThrow(new RuntimeException("Database error"));
        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 200 when pago found")
    void shouldGetPagoById() throws Exception {
        when(pagoService.consultarPago(1)).thenReturn(Optional.of(buildPago()));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404WhenPagoNotFound() throws Exception {
        when(pagoService.consultarPago(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/pagos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should include the associated presupuesto")
    void shouldIncludePresupuestoWhenRetrievingPaymentById() throws Exception {
        when(pagoService.consultarPago(1)).thenReturn(Optional.of(buildPago()));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPresupuesto").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} should return 500 on service error")
    void shouldReturnServerErrorOnGetById() throws Exception {
        when(pagoService.consultarPago(anyInt())).thenThrow(new RuntimeException("Service error"));
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should return pagos for presupuesto")
    void shouldGetPagosByPresupuesto() throws Exception {
        when(pagoService.findPagosByPresupuesto(10)).thenReturn(List.of(buildPago()));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monto").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should include the presupuesto on each entry")
    void shouldIncludePresupuestoOnEachListedPayment() throws Exception {
        when(pagoService.findPagosByPresupuesto(10)).thenReturn(List.of(buildPago()));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPresupuesto").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id} should return 500 on error")
    void shouldReturnServerErrorOnPresupuestoQuery() throws Exception {
        when(pagoService.findPagosByPresupuesto(anyInt())).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return saldo pendiente")
    void shouldGetSaldoPendiente() throws Exception {
        when(pagoService.calcularSaldoPendiente(10)).thenReturn(1000.0f);
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/saldo"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return 404 for invalid presupuesto")
    void shouldReturn404ForInvalidPresupuesto() throws Exception {
        when(pagoService.calcularSaldoPendiente(999)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/999/saldo"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/presupuesto/{id}/saldo should return 500 on error")
    void shouldReturnServerErrorOnSaldoCalculation() throws Exception {
        when(pagoService.calcularSaldoPendiente(anyInt())).thenThrow(new RuntimeException("Calculation error"));
        mockMvc.perform(get("/api/v1/pagos/presupuesto/10/saldo"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/fecha should return pagos in date range")
    void shouldGetPagosByDateRange() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        when(pagoService.findPagosByFechaRange(any(Date.class), any(Date.class)))
                .thenReturn(List.of(buildPago()));

        mockMvc.perform(get("/api/v1/pagos/fecha")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monto").value(500.0));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/fecha should return 500 on service error")
    void shouldReturnServerErrorOnDateRangeQuery() throws Exception {
        when(pagoService.findPagosByFechaRange(any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Query error"));

        mockMvc.perform(get("/api/v1/pagos/fecha")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 201 when pago created")
    void shouldCreatePagoViaJson() throws Exception {
        Pago newPago = buildPago();
        when(pagoService.procesarPago(anyInt(), anyFloat(), any(Date.class), anyString(), any()))
                .thenReturn(newPago);

        String json = """
                {
                    "idPresupuesto": 10,
                    "monto": 500.0,
                    "fecha": "2026-06-16",
                    "observaciones": "Pago de prueba"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value(1));

        verify(pagoService, times(1)).procesarPago(eq(10), eq(500.0f), any(Date.class), eq("Pago de prueba"), any());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return the associated presupuesto when creating a payment")
    void shouldReturnPresupuestoWhenCreatingPayment() throws Exception {
        Pago newPago = buildPago();
        when(pagoService.procesarPago(anyInt(), anyFloat(), any(Date.class), anyString(), any()))
                .thenReturn(newPago);

        String json = """
                {
                    "idPresupuesto": 10,
                    "monto": 500.0,
                    "fecha": "2026-06-16",
                    "observaciones": "Pago de prueba"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPresupuesto").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 400 on validation error")
    void shouldReturn400OnCreateValidationError() throws Exception {
        when(pagoService.procesarPago(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Invalid amount"));

        String json = """
                {
                    "idPresupuesto": 10,
                    "monto": -100.0,
                    "fecha": "2026-06-16",
                    "observaciones": "Invalid"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/pagos should return 500 on service error")
    void shouldReturn500OnCreateServiceError() throws Exception {
        when(pagoService.procesarPago(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenThrow(new RuntimeException("DB error"));

        String json = """
                {
                    "idPresupuesto": 10,
                    "monto": 500.0,
                    "fecha": "2026-06-16",
                    "observaciones": "Test"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should return 201 when pago created via params")
    void shouldCreatePagoViaParams() throws Exception {
        Pago newPago = buildPago();
        when(pagoService.procesarPago(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenReturn(newPago);

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idPresupuesto", "10")
                .param("monto", "500.0")
                .param("fecha", "2026-06-16")
                .param("observaciones", "Test pago"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should handle missing optional params")
    void shouldHandleMissingOptionalParams() throws Exception {
        Pago newPago = buildPago();
        when(pagoService.procesarPago(eq(10), eq(500.0f), isNull(), isNull(), isNull()))
                .thenReturn(newPago);

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idPresupuesto", "10")
                .param("monto", "500.0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/pagos/params should return 500 on service error")
    void shouldReturn500OnParamsServiceError() throws Exception {
        when(pagoService.procesarPago(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/pagos/params")
                .param("idPresupuesto", "10")
                .param("monto", "500.0"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("PUT /api/v1/pagos/{id} should return 200 when updated")
    void shouldUpdatePago() throws Exception {
        Pago updated = buildPago();
        when(pagoService.editarPago(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenReturn(updated);

        String json = """
                {
                    "monto": 600.0,
                    "fecha": "2026-06-16",
                    "observaciones": "Updated"
                }
                """;

        mockMvc.perform(put("/api/v1/pagos/1")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404OnUpdateNotFound() throws Exception {
        when(pagoService.editarPago(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Pago no encontrado"));

        String json = """
                {
                    "monto": 600.0,
                    "fecha": "2026-06-16",
                    "observaciones": "Updated"
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
        when(pagoService.editarPago(anyInt(), anyFloat(), any(), anyString(), any()))
                .thenThrow(new RuntimeException("DB error"));

        String json = """
                {
                    "monto": 600.0,
                    "fecha": "2026-06-16",
                    "observaciones": "Updated"
                }
                """;

        mockMvc.perform(put("/api/v1/pagos/1")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 200 when deleted")
    void shouldDeletePago() throws Exception {
        doNothing().when(pagoService).deletePago(1);
        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isOk());
        verify(pagoService, times(1)).deletePago(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 404 when pago not found")
    void shouldReturn404OnDeleteNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Pago no encontrado")).when(pagoService).deletePago(999);
        mockMvc.perform(delete("/api/v1/pagos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/pagos/{id} should return 500 on service error")
    void shouldReturn500OnDeleteError() throws Exception {
        doThrow(new RuntimeException("DB error")).when(pagoService).deletePago(anyInt());
        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isInternalServerError());
    }
}
