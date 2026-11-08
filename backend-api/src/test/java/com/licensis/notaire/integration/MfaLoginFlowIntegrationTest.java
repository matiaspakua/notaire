package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.domain.mfa.Base32Codec;
import com.licensis.notaire.domain.mfa.TotpAlgorithm;
import com.licensis.notaire.security.LoginAttemptService;
import com.licensis.notaire.testing.RequirementCoverage;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RequirementCoverage({"CU78"})
@SpringBootTest
@ActiveProfiles("test-h2")
@TestPropertySource(properties = {
    "notaire.mfa.policy=ENFORCE",
    "notaire.mfa.encryption-key=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8="
})
@DisplayName("MFA login flow with the real filter chain (CU78, ADR-032)")
class MfaLoginFlowIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;

    @Autowired private WebApplicationContext context;
    @Autowired private LoginAttemptService attempts;
    @Autowired private QueryUsersUseCase users;
    @Autowired private MfaEnrollmentStorePort enrollments;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        attempts.onLoginSucceeded("admin");
    }

    @AfterEach
    void removeEnrolment() {
        users.findByName("admin").ifPresent(admin -> enrollments.deleteByUserId(admin.id()));
    }

    private JsonNode call(String path, String body, String bearer) throws Exception {
        var request = post(path).contentType(APPLICATION_JSON).content(body);
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return JSON.readTree(mvc.perform(request).andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("enforced admin: enrol with the enrolment token, then login needs the code")
    void shouldRunEnrolmentThenChallengeFlow() throws Exception {
        JsonNode first = call("/api/v1/users/login", "{\"name\":\"admin\",\"password\":\"admin\"}", null);
        assertThat(first.get("valid").asBoolean()).isFalse();
        assertThat(first.get("mfaEnrollmentRequired").asBoolean()).isTrue();
        assertThat(first.has("token")).isFalse();
        String enrolToken = first.get("mfaToken").asText();

        mvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + enrolToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/mfa/disable").header(HttpHeaders.AUTHORIZATION, "Bearer " + enrolToken)
                        .contentType(APPLICATION_JSON).content("{\"code\":\"123456\"}"))
                .andExpect(status().isUnauthorized());

        JsonNode begun = call("/api/v1/mfa/enrollment", "", enrolToken);
        byte[] secret = Base32Codec.decode(begun.get("secret").asText());
        String code = TotpAlgorithm.code(secret, TotpAlgorithm.stepAt(Instant.now()));
        JsonNode confirmed = call("/api/v1/mfa/enrollment/confirm", "{\"code\":\"" + code + "\"}", enrolToken);
        assertThat(confirmed.get("recoveryCodes")).hasSize(10);

        JsonNode second = call("/api/v1/users/login", "{\"name\":\"admin\",\"password\":\"admin\"}", null);
        assertThat(second.get("mfaRequired").asBoolean()).isTrue();
        assertThat(second.has("token")).isFalse();
        String challenge = second.get("mfaToken").asText();

        mvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + challenge))
                .andExpect(status().isUnauthorized());

        JsonNode wrong = call("/api/v1/users/login/mfa", "{\"mfaToken\":\"" + challenge + "\",\"code\":\"000000\"}", null);
        assertThat(wrong.get("valid").asBoolean()).isFalse();

        String recovery = confirmed.get("recoveryCodes").get(0).asText();
        JsonNode done = call("/api/v1/users/login/mfa",
                "{\"mfaToken\":\"" + challenge + "\",\"recoveryCode\":\"" + recovery + "\"}", null);
        assertThat(done.get("valid").asBoolean()).isTrue();
        String session = done.get("token").asText();
        mvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + session))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/mfa/disable").header(HttpHeaders.AUTHORIZATION, "Bearer " + session)
                        .contentType(APPLICATION_JSON).content("{\"code\":\"000000\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("mfa endpoints reject anonymous callers")
    void shouldRejectAnonymous() throws Exception {
        mvc.perform(post("/api/v1/mfa/enrollment")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/mfa/disable").contentType(APPLICATION_JSON).content("{\"code\":\"1\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/mfa/enrollment/confirm").contentType(APPLICATION_JSON).content("{\"code\":\"1\"}"))
                .andExpect(status().isUnauthorized());
    }
}
