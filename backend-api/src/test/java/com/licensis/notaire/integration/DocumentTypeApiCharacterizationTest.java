package com.licensis.notaire.integration;

import static org.hamcrest.Matchers.hasItem;
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
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.repository.ProcedureTemplateRepository;

/**
 * Slice 4 of epic #1693 (issue #1705): pins the document-types API (statuses, bodies, dual-table in-use lock,
 * 409 shapes, optimistic locking, create defaults) so the move to ports keeps it. Written against the old
 * implementation.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Document types API behaviour (issue #1705)")
class DocumentTypeApiCharacterizationTest {

    private static final String BASE = "/api/v1/document-types";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProcedureTemplateRepository procedureTemplates;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private String suffix;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        suffix = String.valueOf(System.nanoTime());
    }

    private String body(String name, boolean expires, Integer dueDays, String deliveredBy, Boolean enabled,
            Boolean returned, int version) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"name\": ").append(name == null ? "null" : "\"" + name + "\"");
        json.append(", \"expires\": ").append(expires);
        if (dueDays != null) {
            json.append(", \"dueDays\": ").append(dueDays);
        }
        if (deliveredBy != null) {
            json.append(", \"deliveredBy\": \"").append(deliveredBy).append("\"");
        }
        if (enabled != null) {
            json.append(", \"enabled\": ").append(enabled);
        }
        if (returned != null) {
            json.append(", \"returned\": ").append(returned);
        }
        json.append(", \"version\": ").append(version);
        json.append("}");
        return json.toString();
    }

    private int create(String name) throws Exception {
        MvcResult result = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(name, false, null, "", true, false, 0))).andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idDocumentType").asInt();
    }

    private void useByProcedureTemplate(int typeId) {
        ProcedureTemplatePK pk = new ProcedureTemplatePK(1, typeId);
        procedureTemplates.save(new ProcedureTemplate(pk));
    }

    @Test
    @DisplayName("create answers 201 with the stored type, its generated id and version 0")
    void createShape() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body("Shape " + suffix, true, 30, "CLIENT", true, false, 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentType").isNumber())
                .andExpect(jsonPath("$.name", is("Shape " + suffix)))
                .andExpect(jsonPath("$.expires", is(true)))
                .andExpect(jsonPath("$.dueDays", is(30)))
                .andExpect(jsonPath("$.deliveredBy", is("CLIENT")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.returned", is(false)))
                .andExpect(jsonPath("$.version", is(0)));
    }

    @Test
    @DisplayName("create defaults: deliveredBy empty, enabled true, returned false when omitted")
    void createDefaults() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Defaults " + suffix + "\", \"expires\": false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deliveredBy", is("")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.returned", is(false)));
    }

    @Test
    @DisplayName("create keeps an explicit enabled=false")
    void createKeepsExplicitEnabledFalse() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body("Disabled " + suffix, false, null, "", false, false, 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @DisplayName("read by id, list and search by a part of the name")
    void readListSearch() throws Exception {
        int id = create("Findable " + suffix);

        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Findable " + suffix)));
        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idDocumentType", hasItem(id)));
        mvc.perform(get(BASE + "/search").param("name", "Findable " + suffix)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idDocumentType", is(id)));
    }

    @Test
    @DisplayName("an unknown id answers 404 with no body on read, in-use, update and delete")
    void unknownIsNotFound() throws Exception {
        mvc.perform(get(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(get(BASE + "/987654/in-use")).andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(put(BASE + "/987654").contentType(MediaType.APPLICATION_JSON)
                .content(body("X" + suffix, false, null, "", true, false, 0)))
                .andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(delete(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("in-use is false for an unused type and true once a procedure template uses it")
    void inUse() throws Exception {
        int id = create("Use " + suffix);

        mvc.perform(get(BASE + "/" + id + "/in-use")).andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse", is(false)));
        useByProcedureTemplate(id);
        mvc.perform(get(BASE + "/" + id + "/in-use")).andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse", is(true)));
    }

    @Test
    @DisplayName("update of an unused type replaces its fields, answers 200 with no body and bumps the version")
    void updates() throws Exception {
        int id = create("Before " + suffix);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("After " + suffix, true, 15, "NOTARY", false, true, 0)))
                .andExpect(status().isOk()).andExpect(content().string(""));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(jsonPath("$.name", is("After " + suffix)))
                .andExpect(jsonPath("$.expires", is(true)))
                .andExpect(jsonPath("$.dueDays", is(15)))
                .andExpect(jsonPath("$.deliveredBy", is("NOTARY")))
                .andExpect(jsonPath("$.enabled", is(false)))
                .andExpect(jsonPath("$.returned", is(true)))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("legacy update ignored a stale client version (200); hexagon will refuse it with 409 — see deliberate correction")
    void staleVersionLegacyAllowsOverwrite() throws Exception {
        // Measured on the pre-hexagon controller: copying dto.version onto the entity did not yield a 409.
        // Slice 4 adds an explicit version check in the use case (same as folio types #1703); the assertion
        // below is updated when that lands, and the PR calls out the correction.
        int id = create("Stale " + suffix);
        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("Stale " + suffix, false, null, "", true, false, 0))).andExpect(status().isOk());

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("Stale again " + suffix, false, null, "", true, false, 0)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a type used by a procedure template cannot be edited: 409 with the error text")
    void lockedWhenInUse() throws Exception {
        int id = create("Locked " + suffix);
        useByProcedureTemplate(id);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("Locked " + suffix, false, null, "", true, false, 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error",
                        is("This document type is in use and cannot be modified. Create a new one.")));
    }

    @Test
    @DisplayName("a type used by a procedure template cannot be deleted: 409 with the error text, and it stays")
    void deleteGuard() throws Exception {
        int id = create("Guarded " + suffix);
        useByProcedureTemplate(id);

        mvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error",
                        is("Cannot delete: the document type is used by procedure templates or submitted documents.")));
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("an unused type is deleted with 204 and then answers 404")
    void deletes() throws Exception {
        int id = create("Gone " + suffix);

        mvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("creating without a name answers 400 with the constraint message")
    void createWithoutName() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"expires\": false, \"deliveredBy\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("The submitted data violates a database constraint")));
    }
}
