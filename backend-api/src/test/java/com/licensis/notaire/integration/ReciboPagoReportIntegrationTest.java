package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU15 - Emitir recibo de pago (issue #23).
 */
@RequirementCoverage({"CU15"})
@DisplayName("ReporteController#generarReporteReciboPago — CU15 integration tests")
class ReciboPagoReportIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createCliente() throws Exception {
        String dni = String.valueOf(System.currentTimeMillis() % 1_000_000_000);
        String body = """
                {
                  "nombre": "Ana",
                  "apellido": "Gomez",
                  "dni": "%s",
                  "numeroIdentificacion": "%s",
                  "email": "ana.gomez.%s@example.com",
                  "esCliente": true,
                  "fkIdTipoIdentificacion": {"idTipoIdentificacion": 1}
                }
                """.formatted(dni, dni, dni);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private int createPresupuesto(int idPersona) throws Exception {
        String body = """
                {
                  "fkIdPersona": {"idPersona": %d},
                  "fecha": "2026-09-05",
                  "encabezado": "Presupuesto recibo E2E",
                  "estado": "Pendiente",
                  "monto": 10000.0
                }
                """.formatted(idPersona);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPresupuesto").asInt();
    }

    private void createItem(int idPresupuesto, String nombre) throws Exception {
        String body = """
                {
                  "nombre": "%s",
                  "valor": 10000.0,
                  "tipo": "NORMAL",
                  "fkIdPresupuesto": {"idPresupuesto": %d}
                }
                """.formatted(nombre, idPresupuesto);
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private int createPago(int idPresupuesto, float monto) throws Exception {
        String body = """
                {
                  "idPresupuesto": %d,
                  "monto": %s,
                  "fecha": "2026-09-05",
                  "observaciones": "Pago recibo E2E",
                  "metodoPago": "Transferencia"
                }
                """.formatted(idPresupuesto, monto);
        MvcResult result = mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPago").asInt();
    }

    @Test
    @DisplayName("Should return a PDF recibo for an existing pago")
    void shouldReturnPdfForRecibo() throws Exception {
        int idPersona = createCliente();
        int idPresupuesto = createPresupuesto(idPersona);
        createItem(idPresupuesto, "Escritura de compraventa");
        int idPago = createPago(idPresupuesto, 4000f);

        MvcResult result = mockMvc.perform(get("/api/v1/reportes/recibo-pago/" + idPago))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 404 for a non-existing pago")
    void shouldReturn404ForNonExistingPago() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/recibo-pago/999999"))
                .andExpect(status().isNotFound());
    }
}
