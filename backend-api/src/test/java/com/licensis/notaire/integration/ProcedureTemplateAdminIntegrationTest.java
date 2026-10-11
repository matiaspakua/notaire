package com.licensis.notaire.integration;

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

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #1362: the required-documents checklist per procedure type (CU55). The admin screen adds,
 * edits and removes rows through these endpoints, so a DELETE must really remove the row: it used
 * to answer 204 while the document type's eagerly loaded, cascading template list re-persisted it.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Procedure template admin (issue #1362)")
class ProcedureTemplateAdminIntegrationTest {

    private static final String BASE = "/api/v1/procedure-templates";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static String unique(String prefix) {
        return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    private int createProcedureType() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/procedure-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + unique("PT1362") + "\",\"description\":\"d\"}"))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(res.getResponse().getContentAsString()).get("idProcedureType").asInt();
    }

    private int createDocumentType() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/document-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + unique("DT1362")
                                + "\",\"deliveredBy\":\"Cliente\",\"expires\":false,\"returned\":false}"))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(res.getResponse().getContentAsString()).get("idDocumentType").asInt();
    }

    private void createTemplate(int pt, int dt, String notes) throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fkIdProcedureType\":" + pt + ",\"fkIdDocumentType\":" + dt
                                + ",\"notes\":\"" + notes + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE removes the row: a second GET is 404 and the type's list no longer has it")
    void deleteReallyRemovesTheRow() throws Exception {
        int pt = createProcedureType();
        int dt = createDocumentType();
        createTemplate(pt, dt, "original");

        mockMvc.perform(delete(BASE + "/" + pt + "/" + dt)).andExpect(status().isNoContent());

        mockMvc.perform(get(BASE + "/" + pt + "/" + dt)).andExpect(status().isNotFound());
        mockMvc.perform(get(BASE + "/procedure-types/" + pt)).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(delete(BASE + "/" + pt + "/" + dt)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT changes the notes; adding the same document twice is 409")
    void updateAndDuplicate() throws Exception {
        int pt = createProcedureType();
        int dt = createDocumentType();
        createTemplate(pt, dt, "original");

        mockMvc.perform(put(BASE + "/" + pt + "/" + dt).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Original y copia\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.notes").value("Original y copia"));
        mockMvc.perform(get(BASE + "/procedure-types/" + pt))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].notes").value("Original y copia"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fkIdProcedureType\":" + pt + ",\"fkIdDocumentType\":" + dt + "}"))
                .andExpect(status().isConflict());
    }
}
