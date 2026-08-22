package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RequirementCoverage({"CU47", "CU02"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Presupuesto resumen — CU47 financial summary endpoint")
class PresupuestoResumenControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "Cliente IT", "apellido": "Resumen IT", "numeroIdentificacion": "%s",
                 "esCliente": true, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private Integer createPresupuesto(Integer clienteId, Float montoInmueble) throws Exception {
        String body = """
                {"numero": %d, "fecha": "2026-01-01", "encabezado": "Presupuesto Resumen IT",
                 "estado": "PENDIENTE", "monto": %s, "fkIdPersona": {"idPersona": %d}}
                """.formatted((int) (System.nanoTime() % 100000), montoInmueble, clienteId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPresupuesto").asInt();
    }

    private void createPago(Integer idPresupuesto, Float monto) throws Exception {
        String body = """
                {"idPresupuesto": %d, "monto": %s, "fecha": "2026-08-20", "observaciones": "Pago Resumen IT"}
                """.formatted(idPresupuesto, monto);
        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return the financial summary for a presupuesto with no payments")
    void shouldReturnSummaryForPresupuestoWithoutPayments() throws Exception {
        Integer clienteId = createPersona("51" + (System.nanoTime() % 1000000));
        Integer presupuestoId = createPresupuesto(clienteId, 5000.00f);

        mockMvc.perform(get("/api/v1/presupuestos/" + presupuestoId + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPresupuesto").value(presupuestoId))
                .andExpect(jsonPath("$.total").value(5000.00))
                .andExpect(jsonPath("$.saldoPendiente").value(5000.00))
                .andExpect(jsonPath("$.pagos").isEmpty());
    }

    @Test
    @DisplayName("Should return the financial summary reflecting a registered payment")
    void shouldReturnSummaryForPresupuestoWithPayment() throws Exception {
        Integer clienteId = createPersona("52" + (System.nanoTime() % 1000000));
        Integer presupuestoId = createPresupuesto(clienteId, 5000.00f);
        createPago(presupuestoId, 2000.00f);

        mockMvc.perform(get("/api/v1/presupuestos/" + presupuestoId + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5000.00))
                .andExpect(jsonPath("$.saldoPendiente").value(3000.00))
                .andExpect(jsonPath("$.pagos.length()").value(1));
    }

    @Test
    @DisplayName("Should return 404 when requesting the summary of a non-existent presupuesto")
    void shouldReturnNotFoundForUnknownPresupuesto() throws Exception {
        mockMvc.perform(get("/api/v1/presupuestos/999999/resumen"))
                .andExpect(status().isNotFound());
    }
}
