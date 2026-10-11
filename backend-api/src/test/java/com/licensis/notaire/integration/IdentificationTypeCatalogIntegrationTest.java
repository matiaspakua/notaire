package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.PersonRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #1361 slice 2: the identification-type admin catalog. A type that people use cannot be
 * deleted (409 with a reason, the people stay), and the catalog keeps one row per name in any
 * letter case so the DNI default stays unambiguous.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Identification type admin catalog (issue #1361)")
class IdentificationTypeCatalogIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    @Autowired
    private PersonRepository personRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private int createType(String name) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/identification-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"characters\":\"11\"}"))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(res.getResponse().getContentAsString()).get("idIdentificationType").asInt();
    }

    private int createPerson(int typeId) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ana\",\"lastName\":\"Catalog1361\",\"identificationNumber\":\""
                                + (System.nanoTime() % 100000000L) + "\",\"isClient\":true,\"identificationTypeId\":"
                                + typeId + "}"))
                .andExpect(status().isCreated()).andReturn();
        JsonNode person = mapper.readTree(res.getResponse().getContentAsString());
        return person.get("personId").asInt();
    }

    @Test
    @DisplayName("DELETE of a type used by a person returns 409 with a reason and keeps the type and the person")
    void deleteInUseTypeIsRejected() throws Exception {
        int typeId = createType(unique("USED"));
        int personId = createPerson(typeId);

        mockMvc.perform(delete("/api/v1/identification-types/" + typeId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("in use")));

        assertThat(identificationTypeRepository.existsById(typeId)).isTrue();
        assertThat(personRepository.existsById(personId)).isTrue();
    }

    @Test
    @DisplayName("DELETE of an unused type returns 204")
    void deleteUnusedTypeSucceeds() throws Exception {
        int typeId = createType(unique("FREE"));

        mockMvc.perform(delete("/api/v1/identification-types/" + typeId))
                .andExpect(status().isNoContent());

        assertThat(identificationTypeRepository.existsById(typeId)).isFalse();
    }

    @Test
    @DisplayName("POST with a name that already exists in another letter case returns 409")
    void createDuplicateNameIsRejected() throws Exception {
        String name = unique("DUP");
        createType(name);

        mockMvc.perform(post("/api/v1/identification-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" " + name.toLowerCase() + " \",\"characters\":\"11\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("PUT renaming a type to another type's name returns 409; keeping its own name is fine")
    void renameToExistingNameIsRejected() throws Exception {
        String first = unique("ONE");
        int firstId = createType(first);
        int secondId = createType(unique("TWO"));

        mockMvc.perform(put("/api/v1/identification-types/" + secondId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + first + "\",\"characters\":\"11\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/v1/identification-types/" + firstId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + first + "\",\"characters\":\"13\"}"))
                .andExpect(status().isOk());
    }
}
