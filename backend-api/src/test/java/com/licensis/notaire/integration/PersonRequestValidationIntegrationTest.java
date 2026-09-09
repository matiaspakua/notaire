package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Person request validation (issue #655)")
class PersonRequestValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /people with blank firstName returns 400")
    void shouldRejectCreateWithBlankFirstName() throws Exception {
        mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "", "lastName": "Perez", "identificationNumber": "12345678", "isClient": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /people with missing lastName returns 400")
    void shouldRejectCreateWithMissingLastName() throws Exception {
        mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Ana", "identificationNumber": "12345678", "isClient": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /people with blank identificationNumber returns 400")
    void shouldRejectCreateWithBlankIdentificationNumber() throws Exception {
        mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Ana", "lastName": "Perez", "identificationNumber": "", "isClient": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /people with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Ana", "lastName": "Perez655", "identificationNumber": "99655321", "isClient": true}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /people/{id} with blank firstName returns 400")
    void shouldRejectUpdateWithBlankFirstName() throws Exception {
        mockMvc.perform(put("/api/v1/people/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "", "lastName": "Garcia", "identificationNumber": "20123456", "isClient": false}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /people with a document already registered returns 409 with the existing person's id")
    void shouldRejectCreateWithDuplicateDocument() throws Exception {
        String response = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Otro", "lastName": "Duplicado", "identificationNumber": "20123456", "isClient": true}
                                """))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        java.util.Map<String, Object> body = mapper.readValue(response, java.util.Map.class);
        org.assertj.core.api.Assertions.assertThat(body).containsEntry("existingPersonId", 1);
    }

    @Test
    @DisplayName("PUT /people/{id} with another person's document returns 409")
    void shouldRejectUpdateWithDocumentFromAnotherPerson() throws Exception {
        String createResponse = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Nueva", "lastName": "Person", "identificationNumber": "88888888", "isClient": true}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer newPersonId = (Integer) mapper.readValue(createResponse, java.util.Map.class).get("personId");

        mockMvc.perform(put("/api/v1/people/" + newPersonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Nueva", "lastName": "Person", "identificationNumber": "20123456", "isClient": true}
                                """))
                .andExpect(status().isConflict());
    }
}
