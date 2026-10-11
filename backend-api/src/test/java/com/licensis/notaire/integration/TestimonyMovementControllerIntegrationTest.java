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
 * CU11/CU12/CU44 - Testimony movement circuit (issue #832): entry for
 * registration, recording the inscription, withdrawal, and re-entry.
 */
@RequirementCoverage({"CU11", "CU12", "CU44"})
@DisplayName("TestimonyMovementController — CU11/CU12/CU44 integration tests")
class TestimonyMovementControllerIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createSignedDeed() throws Exception {
        String body = """
                {
                  "number": %d,
                  "body": "Signed deed for a testimony movement",
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
        int idDeed = createSignedDeed();
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
    @DisplayName("Should enter registration for a verified testimony")
    void shouldEnterRegistrationForVerifiedTestimony() throws Exception {
        int idTestimony = generateVerifiedTestimony();

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/enter-registration"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateEntry").exists())
                .andExpect(jsonPath("$.registered").value(false));
    }

    @Test
    @DisplayName("Should return 400 when entering registration for a testimony that is not verified")
    void shouldRejectEnterRegistrationWhenTestimonyNotVerified() throws Exception {
        int idTestimony = generateTestimony();

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/enter-registration"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when entering registration for a testimony that does not exist")
    void shouldReturn404WhenEnterRegistrationForNonExistingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/enter-registration"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when testimony already has an open movement")
    void shouldRejectEnterRegistrationWhenAlreadyOpen() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/enter-registration"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should record the inscription after entry")
    void shouldRecordInscriptionAfterEntry() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/register-inscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(true))
                .andExpect(jsonPath("$.dateRegistration").exists());
    }

    @Test
    @DisplayName("Should return 400 when recording the inscription without a prior entry")
    void shouldRejectInscriptionWithoutEntry() throws Exception {
        int idTestimony = generateVerifiedTestimony();

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/register-inscription"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should withdraw an inscribed testimony")
    void shouldWithdrawInscribedTestimony() throws Exception {
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
    @DisplayName("Should return 400 when withdrawing a testimony that is not inscribed")
    void shouldRejectWithdrawalWhenNotInscribed() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 123}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reenter a previously withdrawn testimony")
    void shouldReenterAfterWithdrawal() throws Exception {
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
    @DisplayName("Should persist the carton number, observation flag, and notes sent on re-entry")
    void shouldPersistReentryData() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);
        registerInscription(idTestimony);
        withdraw(idTestimony, 456);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/reenter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 77, \"observedByRegistry\": true, \"notes\": \"Missing seal\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardNumber").value(77))
                .andExpect(jsonPath("$.observedByRegistry").value(true))
                .andExpect(jsonPath("$.notes").value("Missing seal"));
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
    @DisplayName("Should return 400 when re-entering without a previous withdrawal")
    void shouldRejectReenterWithoutPreviousWithdrawal() throws Exception {
        int idTestimony = generateVerifiedTestimony();
        registerEntry(idTestimony);

        mockMvc.perform(post("/api/v1/testimony-movements/" + idTestimony + "/reenter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when recording the inscription for a testimony that does not exist")
    void shouldReturn404WhenRecordingInscriptionForMissingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/register-inscription"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when withdrawing a testimony that does not exist")
    void shouldReturn404WhenWithdrawingMissingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\": 123}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when re-entering a testimony that does not exist")
    void shouldReturn404WhenReenterForNonExistingTestimony() throws Exception {
        mockMvc.perform(post("/api/v1/testimony-movements/99999/reenter"))
                .andExpect(status().isNotFound());
    }
}
