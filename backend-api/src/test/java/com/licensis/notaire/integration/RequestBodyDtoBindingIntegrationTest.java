package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Request DTO binding / mass-assignment hardening (issue #1068)")
class RequestBodyDtoBindingIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /inmueble ignores client-supplied id and version")
    void shouldIgnoreClientIdAndVersionOnPropertyCreate() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/inmueble")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idProperty": 99999,
                                  "version": 99,
                                  "cadastralDesignation": "NC-1068-001",
                                  "address": "Calle Mass Assignment 1",
                                  "fiscalAppraisal": 1000.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProperty").exists())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("idProperty").asInt()).isNotEqualTo(99999);
        assertThat(body.get("version").asInt()).isNotEqualTo(99);
    }

    @Test
    @DisplayName("Created property returns response DTO with generated id")
    void shouldReturnPropertyResponseDtoWithGeneratedId() throws Exception {
        mockMvc.perform(post("/api/v1/inmueble")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cadastralDesignation": "NC-1068-002",
                                  "address": "Calle Response DTO 2",
                                  "fiscalAppraisal": 2500.5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProperty").isNumber())
                .andExpect(jsonPath("$.cadastralDesignation").value("NC-1068-002"))
                .andExpect(jsonPath("$.fiscalAppraisal").value(2500.5));
    }

    @Test
    @DisplayName("PUT /people/{id} updates from request DTO without applying client version")
    void shouldUpdatePersonFromRequestDtoWithoutClientVersion() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Mass",
                                  "lastName": "Assign1068",
                                  "identificationNumber": "10680001",
                                  "isClient": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        int personId = mapper.readTree(created.getResponse().getContentAsString()).get("personId").asInt();
        int originalVersion = mapper.readTree(created.getResponse().getContentAsString()).get("version").asInt();

        mockMvc.perform(put("/api/v1/people/" + personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": 1,
                                  "version": 999,
                                  "firstName": "MassUpdated",
                                  "lastName": "Assign1068",
                                  "identificationNumber": "10680001",
                                  "isClient": true
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult after = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/api/v1/people/" + personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("MassUpdated"))
                .andReturn();
        JsonNode updated = mapper.readTree(after.getResponse().getContentAsString());
        assertThat(updated.get("personId").asInt()).isEqualTo(personId);
        assertThat(updated.get("version").asInt()).isNotEqualTo(999);
        assertThat(updated.get("version").asInt()).isGreaterThanOrEqualTo(originalVersion);
    }

    @Test
    @DisplayName("POST /presupuestos creates from writable fields and ignores server-managed keys")
    void shouldCreateBudgetFromRequestDtoIgnoringServerFields() throws Exception {
        MvcResult personResult = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Budget",
                                  "lastName": "Client1068",
                                  "identificationNumber": "10680002",
                                  "isClient": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        int personId = mapper.readTree(personResult.getResponse().getContentAsString()).get("personId").asInt();

        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idBudget": 88888,
                                  "version": 77,
                                  "number": 106801,
                                  "date": "2026-10-02",
                                  "encabezado": "Budget mass-assignment",
                                  "status": "Pending",
                                  "propertyAmount": 1500.0,
                                  "person": {"personId": %d}
                                }
                                """.formatted(personId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idBudget").exists())
                // Nested person must carry picker labels (DtoPerson name/lastName) — not personId alone.
                .andExpect(jsonPath("$.person.personId").value(personId))
                .andExpect(jsonPath("$.person.name").value("Budget"))
                .andExpect(jsonPath("$.person.lastName").value("Client1068"))
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("idBudget").asInt()).isNotEqualTo(88888);
        assertThat(body.get("version").asInt()).isNotEqualTo(77);
        assertThat(body.get("encabezado").asText()).isEqualTo("Budget mass-assignment");
    }
}
