package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Suplencia controller — CU23 get by id serializes detached lazy relations")
class SubstitutionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson(String name, String identificationNumber) throws Exception {
        String body = """
                {"firstName": "%s", "lastName": "Substitution IT", "identificationNumber": "%s",
                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                """.formatted(name, identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createSubstitution() throws Exception {
        long suffix = System.nanoTime() % 100000;
        Integer suplente = createPerson("Suplente IT", "410" + suffix);
        Integer suplantado = createPerson("Suplantado IT", "411" + suffix);
        String body = """
                {"dateStart": "2026-01-01", "dateEnd": "2026-01-31",
                 "fkIdSubstitute": {"personId": %d}, "fkIdSubstituted": {"personId": %d}}
                """.formatted(suplente, suplantado);
        MvcResult result = mockMvc.perform(post("/api/v1/suplencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idSubstitution").asInt();
    }

    @Test
    @DisplayName("Should return 200 with suplencia and persons when getting existing suplencia by id")
    void shouldReturnSubstitutionByIdWithPersons() throws Exception {
        Integer id = createSubstitution();

        mockMvc.perform(get("/api/v1/suplencia/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSubstitution").value(id))
                .andExpect(jsonPath("$.fkIdSuplente.personId").isNumber())
                .andExpect(jsonPath("$.fkIdSuplantado.personId").isNumber());
    }

    @Test
    @DisplayName("Should return 404 when getting nonexistent suplencia by id")
    void shouldReturn404ForNonexistentSubstitution() throws Exception {
        mockMvc.perform(get("/api/v1/suplencia/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should actually remove suplencia row so a subsequent get returns 404")
    void shouldDeleteSubstitutionAndMakeItUnreachable() throws Exception {
        Integer id = createSubstitution();

        mockMvc.perform(delete("/api/v1/suplencia/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/suplencia/" + id))
                .andExpect(status().isNotFound());
    }
}
