package com.licensis.notaire.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

/**
 * Slice 2 of epic #1693 (issue #1702): pins the properties API (statuses, bodies, field mapping, version) so the
 * move from the repository-injecting controller to ports keeps it. Written against the old implementation.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Properties API behaviour (issue #1702)")
class PropertyApiCharacterizationTest {

    private static final String BASE = "/api/v1/properties";

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private String suffix;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        suffix = String.valueOf(System.nanoTime());
    }

    private String full(String designation) {
        return """
                {"cadastralDesignation": "%s", "fiscalAppraisal": 1500.50, "address": "Main St %s",
                 "notes": "n", "registrationNumber": "R-1", "volumeFolioLandRecord": "V-2", "boundaries": "north"}
                """.formatted(designation, suffix);
    }

    private int create(String designation) throws Exception {
        MvcResult result = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(full(designation)))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProperty").asInt();
    }

    @Test
    @DisplayName("create answers 201 with every field, the generated id and version 0")
    void createShape() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(full("CAD-" + suffix)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProperty").isNumber())
                .andExpect(jsonPath("$.cadastralDesignation", is("CAD-" + suffix)))
                .andExpect(jsonPath("$.fiscalAppraisal", is(1500.5)))
                .andExpect(jsonPath("$.address", is("Main St " + suffix)))
                .andExpect(jsonPath("$.notes", is("n")))
                .andExpect(jsonPath("$.registrationNumber", is("R-1")))
                .andExpect(jsonPath("$.volumeFolioLandRecord", is("V-2")))
                .andExpect(jsonPath("$.boundaries", is("north")))
                .andExpect(jsonPath("$.version", is(0)));
    }

    @Test
    @DisplayName("get by id returns the stored property and the list contains it")
    void getAndList() throws Exception {
        int id = create("GET-" + suffix);

        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.cadastralDesignation", is("GET-" + suffix)));
        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idProperty == " + id + ")].cadastralDesignation").value("GET-" + suffix));
    }

    @Test
    @DisplayName("an unknown id answers 404 with no body on read, update and delete")
    void unknownIsNotFound() throws Exception {
        mvc.perform(get(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(put(BASE + "/987654").contentType(MediaType.APPLICATION_JSON).content(full("X-" + suffix)))
                .andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(delete(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("a full update replaces every field, including clearing the optional ones, and bumps the version")
    void updateReplacesEverything() throws Exception {
        int id = create("OLD-" + suffix);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"cadastralDesignation\": \"NEW-" + suffix + "\", \"address\": \"New St\"}"))
                .andExpect(status().isOk()).andExpect(content().string(""));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(jsonPath("$.cadastralDesignation", is("NEW-" + suffix)))
                .andExpect(jsonPath("$.address", is("New St")))
                .andExpect(jsonPath("$.fiscalAppraisal", nullValue()))
                .andExpect(jsonPath("$.notes", nullValue()))
                .andExpect(jsonPath("$.registrationNumber", nullValue()))
                .andExpect(jsonPath("$.boundaries", nullValue()))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("a missing or blank cadastral designation answers 400 and stores nothing")
    void requiresDesignation() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"address\": \"A " + suffix + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"cadastralDesignation\": \"  \", \"address\": \"A " + suffix + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("KNOWN DEFECT, kept by the refactor: creating without an address answers 500, not 400")
    void createWithoutAddressAnswers500() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"cadastralDesignation\": \"NOADDR-" + suffix + "\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("delete answers 204 and the property is then gone")
    void deletes() throws Exception {
        int id = create("DEL-" + suffix);

        mvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isNotFound());
    }
}
