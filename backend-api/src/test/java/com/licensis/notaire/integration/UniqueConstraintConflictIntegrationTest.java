package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #579, Owner decision (Oct 9): a request that would create a duplicate (a database
 * unique-constraint violation, SQLState 23505) answers {@code 409 Conflict}; every other
 * constraint violation (NOT NULL, foreign key, check) keeps {@code 400}. The rule applies both
 * to {@code GlobalExceptionHandler} and to the {@code ErrorResponses} helpers.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Unique-constraint violations answer 409 (issue #579)")
class UniqueConstraintConflictIntegrationTest {

    private static final String DUPLICATE_MESSAGE = "The submitted data duplicates an existing record";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createRole(String name) throws Exception {
        String body = mockMvc.perform(post("/api/v1/roles").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = mapper.readTree(body);
        return created.get("idRole").asInt();
    }

    @Test
    @DisplayName("PUT /roles/{id} renaming to an existing role name answers 409 through GlobalExceptionHandler")
    void duplicateOnUpdateIsConflict() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        createRole("dup-a-" + suffix);
        int second = createRole("dup-b-" + suffix);

        String body = mockMvc.perform(put("/api/v1/roles/" + second).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"dup-a-" + suffix + "\",\"active\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(DUPLICATE_MESSAGE))
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(body).get("message").asText())
                .doesNotContain("roles", "dup-a-", "23505", "unique");
    }

    @Test
    @DisplayName("a NOT NULL violation still answers 400 through GlobalExceptionHandler")
    void notNullStillBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-folio").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest(name = "contract of {1} {0} documents 409")
    @CsvSource({"/api/v1/roles/{id}, put", "/api/v1/minutas-inscripcion, post", "/api/v1/cuadernos, post"})
    @DisplayName("operations that can hit a unique constraint document 409")
    void contractDocumentsConflict(String path, String method) throws Exception {
        JsonNode spec = mapper.readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        List<String> codes = new ArrayList<>();
        spec.path("paths").path(path).path(method).path("responses").fieldNames().forEachRemaining(codes::add);
        assertThat(codes).contains("409");
    }
}
