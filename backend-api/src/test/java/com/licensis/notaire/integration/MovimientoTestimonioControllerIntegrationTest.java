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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU11/CU12/CU44 - Circuito de movimientos de testimonio (issue #832): ingreso a
 * inscripción, registro de inscripción, retiro y reingreso.
 */
@RequirementCoverage({"CU11", "CU12", "CU44"})
@DisplayName("MovimientoTestimonioController — CU11/CU12/CU44 integration tests")
class MovimientoTestimonioControllerIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createEscrituraFirmada() throws Exception {
        String body = """
                {
                  "numero": %d,
                  "cuerpo": "Escritura firmada para movimiento de testimonio",
                  "estado": "Firmada",
                  "fechaEscrituracion": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000));
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idEscritura").asInt();
    }

    private int generarTestimonio() throws Exception {
        int idEscritura = createEscrituraFirmada();
        MvcResult result = mockMvc.perform(post("/api/v1/testimonio/" + idEscritura + "/generar"))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idTestimonio").asInt();
    }

    private int generarTestimonioVerificado() throws Exception {
        int idTestimonio = generarTestimonio();
        mockMvc.perform(post("/api/v1/testimonio/" + idTestimonio + "/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observado\": false}"))
                .andExpect(status().isOk());
        return idTestimonio;
    }

    private void ingresarInscripcion(int idTestimonio) throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/ingresar-inscripcion"))
                .andExpect(status().isCreated());
    }

    private void registrarInscripcion(int idTestimonio) throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/registrar-inscripcion"))
                .andExpect(status().isOk());
    }

    private void retirar(int idTestimonio, int numeroCarton) throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCarton\": " + numeroCarton + "}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should ingresar a inscripción a verified testimonio")
    void shouldIngresarInscripcionForVerifiedTestimonio() throws Exception {
        int idTestimonio = generarTestimonioVerificado();

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/ingresar-inscripcion"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fechaIngreso").exists())
                .andExpect(jsonPath("$.inscripta").value(false));
    }

    @Test
    @DisplayName("Should return 400 when ingresando a inscripción a non-verified testimonio")
    void shouldRejectIngresarInscripcionWhenTestimonioNotVerified() throws Exception {
        int idTestimonio = generarTestimonio();

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/ingresar-inscripcion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when ingresando a inscripción a non-existing testimonio")
    void shouldReturn404WhenIngresarInscripcionForNonExistingTestimonio() throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio/99999/ingresar-inscripcion"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when testimonio already has an open movement")
    void shouldRejectIngresarInscripcionWhenAlreadyOpen() throws Exception {
        int idTestimonio = generarTestimonioVerificado();
        ingresarInscripcion(idTestimonio);

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/ingresar-inscripcion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should registrar inscripción after ingreso")
    void shouldRegistrarInscripcionAfterIngreso() throws Exception {
        int idTestimonio = generarTestimonioVerificado();
        ingresarInscripcion(idTestimonio);

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/registrar-inscripcion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inscripta").value(true))
                .andExpect(jsonPath("$.fechaInscripcion").exists());
    }

    @Test
    @DisplayName("Should return 400 when registrando inscripción without a prior ingreso")
    void shouldRejectRegistrarInscripcionWithoutIngreso() throws Exception {
        int idTestimonio = generarTestimonioVerificado();

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/registrar-inscripcion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should retirar an inscripto testimonio")
    void shouldRetirarInscriptoTestimonio() throws Exception {
        int idTestimonio = generarTestimonioVerificado();
        ingresarInscripcion(idTestimonio);
        registrarInscripcion(idTestimonio);

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCarton\": 123}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaSalida").exists())
                .andExpect(jsonPath("$.numeroCarton").value(123));
    }

    @Test
    @DisplayName("Should return 400 when retirando a testimonio that is not inscripto")
    void shouldRejectRetirarWhenNotInscripto() throws Exception {
        int idTestimonio = generarTestimonioVerificado();
        ingresarInscripcion(idTestimonio);

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCarton\": 123}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reingresar a previously withdrawn testimonio")
    void shouldReingresarAfterRetiro() throws Exception {
        int idTestimonio = generarTestimonioVerificado();
        ingresarInscripcion(idTestimonio);
        registrarInscripcion(idTestimonio);
        retirar(idTestimonio, 456);

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/reingresar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fechaIngreso").exists())
                .andExpect(jsonPath("$.inscripta").value(false));
    }

    @Test
    @DisplayName("Should return 400 when reingresando without a previous retiro")
    void shouldRejectReingresarWithoutPreviousRetiro() throws Exception {
        int idTestimonio = generarTestimonioVerificado();
        ingresarInscripcion(idTestimonio);

        mockMvc.perform(post("/api/v1/movimiento-testimonio/" + idTestimonio + "/reingresar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when registrando inscripción for a non-existing testimonio")
    void shouldReturn404WhenRegistrarInscripcionForNonExistingTestimonio() throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio/99999/registrar-inscripcion"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when retirando a non-existing testimonio")
    void shouldReturn404WhenRetirarForNonExistingTestimonio() throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio/99999/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCarton\": 123}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when reingresando a non-existing testimonio")
    void shouldReturn404WhenReingresarForNonExistingTestimonio() throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio/99999/reingresar"))
                .andExpect(status().isNotFound());
    }
}
