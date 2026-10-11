package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #1364: testimony edit and delete from the UI. A testimony that already has registry
 * movements (CU11/CU12/CU44) is part of the deed's history, so DELETE answers 409 with a reason and
 * keeps the testimony and its movements. It used to answer 204 and cascade-delete the movements.
 * A testimony without movements is deleted, and PUT edits number and notes.
 */
@RequirementCoverage({"CU07", "CU44"})
@DisplayName("Testimony edit/delete keeps the movement history (issue #1364)")
class TestimonyDeleteKeepsHistoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int generateVerifiedTestimony() throws Exception {
        String deed = """
                {"number": %d, "body": "Signed deed #1364", "status": "Firmada", "dateDeedrecording": "2026-06-16"}
                """.formatted((int) (System.nanoTime() % 1_000_000));
        MvcResult d = mockMvc.perform(post("/api/v1/deeds").contentType(MediaType.APPLICATION_JSON).content(deed))
                .andExpect(status().isCreated()).andReturn();
        int idDeed = mapper.readTree(d.getResponse().getContentAsString()).get("idDeed").asInt();
        MvcResult t = mockMvc.perform(post("/api/v1/testimonies/" + idDeed + "/generate"))
                .andExpect(status().isCreated()).andReturn();
        int id = mapper.readTree(t.getResponse().getContentAsString()).get("idTestimony").asInt();
        mockMvc.perform(post("/api/v1/testimonies/" + id + "/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"flagged\": false}")).andExpect(status().isOk());
        return id;
    }

    private boolean movementsOf(int idTestimony) throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v1/testimony-movements")).andExpect(status().isOk()).andReturn();
        for (JsonNode m : mapper.readTree(res.getResponse().getContentAsString())) {
            if (m.path("testimony").path("idTestimony").asInt() == idTestimony) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("DELETE of a testimony with movements answers 409 with a reason and keeps the movements")
    void deleteWithMovementsIsRejected() throws Exception {
        int id = generateVerifiedTestimony();
        mockMvc.perform(post("/api/v1/testimony-movements/" + id + "/enter-registration"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/testimonies/" + id))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("movements")));

        mockMvc.perform(get("/api/v1/testimonies/" + id)).andExpect(status().isOk());
        assertThat(movementsOf(id)).as("movement history kept").isTrue();
    }

    @Test
    @DisplayName("DELETE of a testimony without movements answers 204 and GET answers 404")
    void deleteWithoutMovements() throws Exception {
        int id = generateVerifiedTestimony();
        mockMvc.perform(delete("/api/v1/testimonies/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/testimonies/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT edits number and notes and keeps the deed")
    void putEditsNumberAndNotes() throws Exception {
        int id = generateVerifiedTestimony();
        JsonNode before = mapper.readTree(mockMvc.perform(get("/api/v1/testimonies/" + id))
                .andReturn().getResponse().getContentAsString());
        int newNumber = before.get("number").asInt() + 7;
        mockMvc.perform(put("/api/v1/testimonies/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\": " + newNumber + ", \"notes\": \"Corrected\", \"flagged\": false, \"verified\": "
                                + before.get("verified").asBoolean() + ", \"version\": " + before.get("version").asInt() + "}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/testimonies/" + id))
                .andExpect(jsonPath("$.number").value(newNumber))
                .andExpect(jsonPath("$.notes").value("Corrected"))
                .andExpect(jsonPath("$.deed.idDeed").value(before.get("deed").get("idDeed").asInt()));
    }
}
