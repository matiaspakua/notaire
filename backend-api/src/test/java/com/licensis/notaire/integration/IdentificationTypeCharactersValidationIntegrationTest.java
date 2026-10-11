package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #1526: {@code characters} (the number length the person form uses) is required and must be
 * a whole number from 1 to 50. A missing value used to reach the NOT NULL column and answer 500, and
 * any text was stored.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Identification type characters validation (issue #1526)")
class IdentificationTypeCharactersValidationIntegrationTest {

    private static final String BASE = "/api/v1/identification-types";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static String unique() {
        return "Chars1526 " + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("POST without characters answers 400, not 500")
    void missingCharactersIsBadRequest() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + unique() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest(name = "characters=\"{0}\" is rejected")
    @ValueSource(strings = {"", "  ", "abc", "0", "51", "-3", "8.5", "007"})
    @DisplayName("POST with characters that aren't a whole number from 1 to 50 answers 400")
    void invalidCharactersIsBadRequest(String characters) throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + unique() + "\",\"characters\":\"" + characters + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "characters=\"{0}\" is accepted")
    @ValueSource(strings = {"1", "8", "11", "50"})
    @DisplayName("POST with a whole number from 1 to 50 is created")
    void validCharactersIsCreated(String characters) throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + unique() + "\",\"characters\":\"" + characters + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.characters").value(characters));
    }

    @Test
    @DisplayName("PUT without characters answers 400 and keeps the stored value")
    void putWithoutCharactersIsBadRequest() throws Exception {
        String name = unique();
        MvcResult created = mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"characters\":\"8\"}"))
                .andExpect(status().isCreated()).andReturn();
        int id = mapper.readTree(created.getResponse().getContentAsString()).get("idIdentificationType").asInt();

        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"characters\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
