package com.licensis.notaire.integration;

import static org.hamcrest.Matchers.is;
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
 * Slice 1 of epic #1693 (issue #1700): pins the parts of the identification-types API that the other tests
 * leave open (empty 404 bodies, the 409 error bodies, the response shape) so the move to ports keeps them.
 * Written against the old implementation.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Identification types API behaviour (issue #1700)")
class IdentificationTypeApiCharacterizationTest {

    private static final String BASE = "/api/v1/identification-types";

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

    private String body(String name, String characters) {
        return "{\"name\": \"" + name + "\", \"characters\": \"" + characters + "\"}";
    }

    private int create(String name) throws Exception {
        MvcResult result = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body(name, "9")))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idIdentificationType").asInt();
    }

    @Test
    @DisplayName("create answers 201 with id, trimmed name, characters and version")
    void createShape() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("  Shape " + suffix + "  ", "12")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idIdentificationType").isNumber())
                .andExpect(jsonPath("$.name", is("Shape " + suffix)))
                .andExpect(jsonPath("$.characters", is("12")))
                .andExpect(jsonPath("$.version", is(0)));
    }

    @Test
    @DisplayName("get by id returns the stored type; an unknown id answers 404 with no body")
    void getAndNotFound() throws Exception {
        int id = create("Get " + suffix);

        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Get " + suffix)));
        mvc.perform(get(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("update changes name and characters and answers 200 with no body; version increases")
    void updates() throws Exception {
        int id = create("Before " + suffix);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body("After " + suffix, "11")))
                .andExpect(status().isOk()).andExpect(content().string(""));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(jsonPath("$.name", is("After " + suffix)))
                .andExpect(jsonPath("$.characters", is("11")))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("update and delete of an unknown id answer 404 with no body")
    void updateAndDeleteUnknown() throws Exception {
        mvc.perform(put(BASE + "/987654").contentType(MediaType.APPLICATION_JSON).content(body("X" + suffix, "9")))
                .andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(delete(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("a duplicate name answers 409 with the standard error body and the reason")
    void duplicateBody() throws Exception {
        create("Dup " + suffix);

        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body(" dup " + suffix, "9")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("An identification type with this name already exists")))
                .andExpect(jsonPath("$.path", is(BASE)));
    }

    @Test
    @DisplayName("deleting a type used by people answers 409 with the reason and keeps the type")
    void inUseBody() throws Exception {
        int id = create("Used " + suffix);
        String person = "{\"firstName\": \"Used\", \"lastName\": \"Type" + suffix + "\", "
                + "\"identificationNumber\": \"" + suffix.substring(suffix.length() - 8) + "\", \"isClient\": true, "
                + "\"identificationTypeId\": " + id + "}";
        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(person))
                .andExpect(status().isCreated());

        mvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Cannot delete: the identification type is in use by people")))
                .andExpect(jsonPath("$.path", is(BASE + "/" + id)));
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("an unused type is deleted with 204 and then answers 404")
    void deletes() throws Exception {
        int id = create("Gone " + suffix);

        mvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent());
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isNotFound());
    }
}
