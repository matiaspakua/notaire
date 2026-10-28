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
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import com.licensis.notaire.repository.PersonRepository;

/**
 * Slice 3 of epic #1693 (issue #1703): pins the folio-types API (statuses, bodies, the in-use lock, the 409 shapes,
 * optimistic locking on the client version) so the move to ports keeps it. Written against the old implementation.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Folio types API behaviour (issue #1703)")
class FolioTypeApiCharacterizationTest {

    private static final String BASE = "/api/v1/folio-types";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FolioTypeRepository folioTypes;

    @Autowired
    private FolioRepository folios;

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

    private String body(String name, String notes, boolean enabled, boolean auxiliary, int version) {
        return "{\"name\": \"" + name + "\", \"notes\": " + (notes == null ? "null" : "\"" + notes + "\"")
                + ", \"enabled\": " + enabled + ", \"isAuxiliary\": " + auxiliary + ", \"version\": " + version + "}";
    }

    private int create(String name) throws Exception {
        MvcResult result = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(name, "note", true, false, 0))).andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idFolioType").asInt();
    }

    private void useByAFolio(int typeId) {
        Folio folio = new Folio();
        folio.setNumber((int) (System.nanoTime() % 1_000_000));
        folio.setYear(2026);
        folio.setStatus("New");
        folio.setFkIdFolioType(folioTypes.findById(typeId).orElseThrow());
        folio.setFkIdNotaryPerson(people.findById(1).orElseThrow());
        folios.save(folio);
    }

    @Test
    @DisplayName("create answers 201 with the stored type, its generated id and version 0")
    void createShape() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Shape " + suffix, "n", true, true, 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idFolioType").isNumber())
                .andExpect(jsonPath("$.name", is("Shape " + suffix)))
                .andExpect(jsonPath("$.notes", is("n")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.version", is(0)));
    }

    @Test
    @DisplayName("read by id, list and search by a part of the name")
    void readListSearch() throws Exception {
        int id = create("Findable " + suffix);

        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Findable " + suffix)));
        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idFolioType", hasItem(id)));
        mvc.perform(get(BASE + "/search").param("name", "Findable " + suffix)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idFolioType", is(id)));
    }

    @Test
    @DisplayName("an unknown id answers 404 with no body on read, in-use, update and delete")
    void unknownIsNotFound() throws Exception {
        mvc.perform(get(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(get(BASE + "/987654/in-use")).andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(put(BASE + "/987654").contentType(MediaType.APPLICATION_JSON)
                .content(body("X" + suffix, null, true, false, 0)))
                .andExpect(status().isNotFound()).andExpect(content().string(""));
        mvc.perform(delete(BASE + "/987654")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("in-use is false for an unused type and true once a folio uses it")
    void inUse() throws Exception {
        int id = create("Use " + suffix);

        mvc.perform(get(BASE + "/" + id + "/in-use")).andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse", is(false)));
        useByAFolio(id);
        mvc.perform(get(BASE + "/" + id + "/in-use")).andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse", is(true)));
    }

    @Test
    @DisplayName("update of an unused type replaces its fields, answers 200 with no body and bumps the version")
    void updates() throws Exception {
        int id = create("Before " + suffix);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("After " + suffix, "changed", false, true, 0)))
                .andExpect(status().isOk()).andExpect(content().string(""));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(jsonPath("$.name", is("After " + suffix)))
                .andExpect(jsonPath("$.notes", is("changed")))
                .andExpect(jsonPath("$.enabled", is(false)))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("update with a version that is not the stored one answers 409 with the stale-version message")
    void staleVersion() throws Exception {
        int id = create("Stale " + suffix);
        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("Stale " + suffix, "v1", true, false, 0))).andExpect(status().isOk());

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("Stale " + suffix, "again", true, false, 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("The record was modified by another user; reload it and try again")));
    }

    @Test
    @DisplayName("a type used by folios cannot be edited: 409 with the error text")
    void lockedWhenInUse() throws Exception {
        int id = create("Locked " + suffix);
        useByAFolio(id);

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(body("Locked " + suffix, "x", true, false, 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("This folio type is in use and cannot be modified. Create a new one.")));
    }

    @Test
    @DisplayName("a type used by folios cannot be deleted: 409 with the error text, and it stays")
    void deleteGuard() throws Exception {
        int id = create("Guarded " + suffix);
        useByAFolio(id);

        mvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Cannot delete: the folio type is in use by folios.")));
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
                .content("{\"notes\": \"no name " + suffix + "\", \"enabled\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("name: must not be blank")));
    }
}
