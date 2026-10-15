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

import java.util.Date;

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
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.domain.management.ManagementStatusInUseException;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.PersonRepository;

/**
 * Slice 5 of epic #1693 (issue #1719): pins the management-statuses API (statuses, bodies, in-use lock, 409 shapes,
 * optimistic locking, name constraint) so the move to ports keeps it. Written against the old implementation.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Management statuses API behaviour (issue #1719)")
class ManagementStatusApiCharacterizationTest {

    private static final String BASE = "/api/v1/management-statuses";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ManagementStatusRepository statuses;

    @Autowired
    private DeedManagementRepository deedManagements;

    @Autowired
    private PersonRepository people;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private String suffix;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        suffix = String.valueOf(System.nanoTime());
    }

    private String body(String name, String notes, Integer version) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"name\": ").append(name == null ? "null" : "\"" + name + "\"");
        if (notes != null) {
            json.append(", \"notes\": \"").append(notes).append("\"");
        }
        if (version != null) {
            json.append(", \"version\": ").append(version);
        }
        json.append("}");
        return json.toString();
    }

    private int create(String name) throws Exception {
        MvcResult result = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(body(name, "note", 0)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idManagementStatus").asInt();
    }

    private void useByDeedManagement(int statusId) {
        Person notary = people.findById(1).orElseThrow();
        ManagementStatus status = statuses.findById(statusId).orElseThrow();

        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        management.setNumber(90000 + statusId);
        management.setHeading("Test management for status " + statusId);
        management.setFkIdNotaryPerson(notary);
        management.setFkIdManagementStatus(status);
        deedManagements.save(management);
    }

    @Test
    @DisplayName("create answers 201 with the stored status, its generated id and version 0")
    void createShape() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Shape " + suffix, "n", 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idManagementStatus").isNumber())
                .andExpect(jsonPath("$.name", is("Shape " + suffix)))
                .andExpect(jsonPath("$.notes", is("n")))
                .andExpect(jsonPath("$.version", is(0)));
    }

    @Test
    @DisplayName("read by id, list and search by a part of the name")
    void readListSearch() throws Exception {
        int id = create("Findable " + suffix);

        mvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Findable " + suffix)));
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$[*].idManagementStatus", hasItem(id)));
        mvc.perform(get(BASE + "/search").param("name", "Findable " + suffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idManagementStatus", is(id)));
    }

    @Test
    @DisplayName("an unknown id answers 404 with no body on read, in-use, update and delete")
    void unknownIsNotFound() throws Exception {
        mvc.perform(get(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(get(BASE + "/987654/in-use")).andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(put(BASE + "/987654").contentType(MediaType.APPLICATION_JSON).content(body("X" + suffix, null, 0)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        mvc.perform(delete(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("in-use is false for an unused status and true once a deed management uses it")
    void inUse() throws Exception {
        int id = create("Use " + suffix);

        mvc.perform(get(BASE + "/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse", is(false)));
        useByDeedManagement(id);
        mvc.perform(get(BASE + "/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse", is(true)));
    }

    @Test
    @DisplayName("update of an unused status replaces its fields, answers 200 with no body and bumps the version")
    void updates() throws Exception {
        int id = create("Before " + suffix);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("After " + suffix, "changed", 0)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(jsonPath("$.name", is("After " + suffix)))
                .andExpect(jsonPath("$.notes", is("changed")))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("update with a version that is not the stored one answers 409 with the stale-version message")
    void staleVersion() throws Exception {
        // Deliberate correction vs pre-hexagon controller: copying dto.version onto the entity did not
        // yield a 409. The use case now compares versions explicitly (same as document types #1705).
        // First put must dirty a field so the persistence @Version actually bumps.
        int id = create("Stale " + suffix);
        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Stale " + suffix, "CHANGED", 0)))
                .andExpect(status().isOk());

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Stale again " + suffix, "CHANGED", 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message",
                        is("The record was modified by another user; reload it and try again")));
    }

    @Test
    @DisplayName("a status used by a deed management cannot be edited: 409 with the error text")
    void lockedWhenInUse() throws Exception {
        int id = create("Locked " + suffix);
        useByDeedManagement(id);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Locked " + suffix, "note", 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is(ManagementStatusInUseException.LOCKED_MESSAGE)));
    }

    @Test
    @DisplayName("a status used by a deed management cannot be deleted: 409 with the error text, and it stays")
    void deleteGuard() throws Exception {
        int id = create("Guarded " + suffix);
        useByDeedManagement(id);

        mvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is(ManagementStatusInUseException.DELETE_MESSAGE)));
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("an unused status is deleted with 204 and then answers 404")
    void deletes() throws Exception {
        int id = create("Gone " + suffix);

        mvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("creating without a name answers 400 with the constraint message")
    void createWithoutName() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"notes\": \"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("The submitted data violates a database constraint")));
    }
}
