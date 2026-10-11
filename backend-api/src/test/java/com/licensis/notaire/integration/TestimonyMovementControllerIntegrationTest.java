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
 * CU11/CU12/CU44 - Circuito of movimientos of testimony (issue #832): ingreso a
 * registration, registro of registration, retiro and reentry.
 */
@RequirementCoverage({"CU11", "CU12", "CU44"})
@DisplayName("MovimientoTestimonioController — CU11/CU12/CU44 integration tests")
class TestimonyMovementControllerIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createDeedFirmada() throws Exception {
        String body = """
                {
                  "number": %d,
                  "body": "Deed firmada para movement de testimony",
                  "status": "Firmada",
                  "dateDeedrecording": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000));
        MvcResult result = mockMvc.perform(post("/api/v1/deeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idDeed").asInt();
    }

    private int generateTestimony() throws Exception {
        int idDeed = createDeedFirmada();
        MvcResult result = mockMvc.perform(post("/api/v1/testimonies/" + idDeed + "/generate"))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idTestimony").asInt();
    }

    private int generateVerifiedTestimony() throws Exception {
        int idTestimony = generateTestimony();
        mockMvc.perform(post("/api/v1/testimonies/" + idTestimony + "/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flagged\": false}"))
                .andExpect(status().isOk());
        return idTestimony;
    }

    private void registerEntry(int idTestimony) throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/enter-registration"))
                .andExpect(status().isCreated());
    }

    private void registerInscription(int idTestimony) throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/register-inscription"))
                .andExpect(status().isOk());
    }

    private void withdraw(int idTestimony, int cardNumber) throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": " + cardNumber + "}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should enter registration a verified testimony")
    void shouldIngresarRegistrationForVerifiedTestimony() throws Exception {
        int idTestimony = generateVerifiedTestimony();

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/enter-registration"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateEntry").exists())
                .andExpect(jsonPath("$.registered").value(false));
    }

    @Test
    @DisplayName("Should return 400 when ingresando a registration a non-verified testimony")
    void shouldRejectIngresarRegistrationWhenTestimonyNotVerified() throws Exception {
        int idTestimony = generateTestimony();

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/enter-registration"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when ingresando a registration a non-existing testimony")
    void shouldReturn404WhenIngresarRegistrationForNonExistingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/enter-registration"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when testimony already has an open movement")
    void shouldRejectIngresarRegistrationWhenAlreadyOpen() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/enter-registration"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should record registration after ingreso")
    void shouldRegistrarRegistrationAfterEntry() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/register-inscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(true))
                .andExpect(jsonPath("$.dateRegistration").exists());
    }

    @Test
    @DisplayName("Should return 400 when registrando registration without a prior ingreso")
    void shouldRejectRegistrarRegistrationWithoutEntry() throws Exception {
        int idTestimony = generateVerifiedTestimony();

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/register-inscription"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should withdraw an inscripto testimony")
    void shouldRetirarInscriptoTestimony() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);
        registerInscription(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 123}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateExit").exists())
                .andExpect(jsonPath("$.cardNumber").value(123));
    }

    @Test
    @DisplayName("Should return 400 when retirando a testimony that is not inscripto")
    void shouldRejectRetirarWhenNotInscripto() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 123}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reenter a previously withdrawn testimony")
    void shouldReingresarAfterWithdrawal() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);
        registerInscription(idTestimony);
        withdraw(idTestimony, 456);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/reenter"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateEntry").exists())
                .andExpect(jsonPath("$.registered").value(false))
                .andExpect(jsonPath("$.cardNumber").value(0))
                .andExpect(jsonPath("$.observedByRegistry").value(false));
    }

    @Test
    @DisplayName("Should persist cartón number, observation flag and notes sent on reentry")
    void shouldPersistReentryData() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);
        registerInscription(idTestimony);
        withdraw(idTestimony, 456);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/reenter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 77, \"observedByRegistry\": true, \"notes\": \"Falta sello\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardNumber").value(77))
                .andExpect(jsonPath("$.observedByRegistry").value(true))
                .andExpect(jsonPath("$.notes").value("Falta sello"));
    }

    @Test
    @DisplayName("Should return 400 when the reentry is observed but has no notes")
    void shouldRejectObservedReentryWithoutNotes() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);
        registerInscription(idTestimony);
        withdraw(idTestimony, 456);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/reenter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 77, \"observedByRegistry\": true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when reingresando without a previous retiro")
    void shouldRejectReingresarWithoutPreviousWithdrawal() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/reenter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when registrando registration for a non-existing testimony")
    void shouldReturn404WhenRegistrarRegistrationForNonExistingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/register-inscription"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when retirando a non-existing testimony")
    void shouldReturn404WhenRetirarForNonExistingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 123}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when reingresando a non-existing testimony")
    void shouldReturn404WhenReingresarForNonExistingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/reenter"))
                .andExpect(status().isNotFound());
    }
}
