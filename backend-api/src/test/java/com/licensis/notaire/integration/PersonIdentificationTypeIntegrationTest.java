package com.licensis.notaire.integration;

import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.IdentificationTypeRepository;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #1361: every new person has an identification type. Omitted on create it defaults to DNI
 * (Owner decision); an id that is not in the catalog is a 400, not a silent DNI.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Person identification type (issue #1361)")
class PersonIdentificationTypeIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    private int dniId;
    private int cuitId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        dniId = type("DNI");
        cuitId = type("CUIT");
    }

    private int type(String name) {
        return identificationTypeRepository.findByName(name).orElseGet(() -> {
            IdentificationType t = new IdentificationType();
            t.setName(name);
            return identificationTypeRepository.save(t);
        }).getIdIdentificationType();
    }

    private static String body(String number, String typeField) {
        return "{\"firstName\":\"Ana\",\"lastName\":\"Type1361\",\"identificationNumber\":\"" + number + "\","
                + "\"isClient\":true" + (typeField == null ? "" : "," + typeField) + "}";
    }

    @Test
    @DisplayName("POST /people with identificationTypeId CUIT stores CUIT")
    void createWithExplicitTypeStoresIt() throws Exception {
        mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                        .content(body("20136100011", "\"identificationTypeId\":" + cuitId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificationTypeId").value(cuitId));
    }

    @Test
    @DisplayName("POST /people without a type defaults to DNI")
    void createWithoutTypeDefaultsToDni() throws Exception {
        mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                        .content(body("13610002", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificationTypeId").value(dniId));
    }

    @Test
    @DisplayName("POST /people with an unknown identificationTypeId returns 400 naming the field")
    void createWithUnknownTypeIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                        .content(body("13610003", "\"identificationTypeId\":987654")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("identificationTypeId")));
    }

    @Test
    @DisplayName("POST /people with an unknown fkIdIdentificationType returns 400")
    void createWithUnknownNestedTypeIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                        .content(body("13610004", "\"fkIdIdentificationType\":{\"idIdentificationType\":987654}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /people/{id} with an unknown identificationTypeId returns 400 and keeps the stored type")
    void updateWithUnknownTypeIsRejected() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                        .content(body("20136100051", "\"identificationTypeId\":" + cuitId)))
                .andExpect(status().isCreated()).andReturn();
        JsonNode person = mapper.readTree(created.getResponse().getContentAsString());
        int id = person.get("personId").asInt();

        mockMvc.perform(put("/api/v1/people/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("20136100051", "\"identificationTypeId\":987654")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/people/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("20136100051", "\"identificationTypeId\":" + dniId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificationTypeId").value(dniId));
    }
}
