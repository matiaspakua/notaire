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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #579 (slice 3, workflow controllers) and issue #655 (empty bodies): a workflow
 * definition, node or transition create or update with missing or invalid data is a client
 * error. The workflow controllers answered 409 on a NOT NULL violation, 500 when a required id
 * was missing (the repository rejects a null id) or an update broke a constraint, and 200 for a
 * node update with nothing to change. All of these must answer 400 with the standard
 * {@code ErrorResponse}, and the contract must document 400 on POST and PUT.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Workflow create/update with missing or invalid data answers 400 (issues #579, #655)")
class WorkflowConstraintErrorsIntegrationTest {

    /** Same text as {@code GlobalExceptionHandler} for a {@code DataIntegrityViolationException}. */
    private static final String CONSTRAINT_MESSAGE = "The submitted data violates a database constraint";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private ResultActions send(String method, String url, String body) throws Exception {
        var builder = "put".equals(method) ? put(url) : post(url);
        return mockMvc.perform(builder.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode created(String url, String body) throws Exception {
        return mapper.readTree(send("post", url, body).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private int workflow() throws Exception {
        return created("/api/v1/workflow-definition", "{\"name\":\"WF579-" + System.nanoTime() + "\"}")
                .get("id").asInt();
    }

    private int managementStatus() throws Exception {
        return created("/api/v1/estado-gestion", "{\"name\":\"E579-" + System.nanoTime() + "\"}")
                .get("idManagementStatus").asInt();
    }

    private int node(int workflowId, int statusId, String type) throws Exception {
        return created("/api/v1/workflow-node", "{\"workflowDefinitionId\":" + workflowId
                + ",\"statusManagementId\":" + statusId + ",\"type\":\"" + type + "\"}").get("id").asInt();
    }

    private static void badRequest(ResultActions result, String messagePart) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString(messagePart)));
    }

    @Test
    @DisplayName("POST /workflow-definition without a name answers 400, not 409")
    void definitionCreateWithoutName() throws Exception {
        badRequest(send("post", "/api/v1/workflow-definition", "{}"), CONSTRAINT_MESSAGE);
    }

    @Test
    @DisplayName("PUT /workflow-definition/{id} without a name answers 400, not 500")
    void definitionUpdateWithoutName() throws Exception {
        int id = workflow();
        badRequest(send("put", "/api/v1/workflow-definition/" + id, "{\"description\":\"x\"}"),
                CONSTRAINT_MESSAGE);
    }

    @ParameterizedTest(name = "POST /workflow-node {0} answers 400 naming {1}")
    @CsvSource(delimiter = '|', value = {
        "{}|workflowDefinitionId",
        "{\"workflowDefinitionId\":WF}|statusManagementId",
        "{\"workflowDefinitionId\":WF,\"statusManagementId\":ST}|type",
        "{\"workflowDefinitionId\":WF,\"statusManagementId\":ST,\"type\":\"BOGUS\"}|type"
    })
    @DisplayName("a node create with a missing or invalid field answers 400, not 500/409")
    void nodeCreateMissingOrInvalidField(String body, String field) throws Exception {
        String json = body.replace("WF", String.valueOf(workflow())).replace("ST", String.valueOf(managementStatus()));
        badRequest(send("post", "/api/v1/workflow-node", json), field);
    }

    @ParameterizedTest(name = "PUT /workflow-node/'{id}' {0} answers 400")
    @CsvSource(delimiter = '|', value = {"{}|type", "{\"type\":\"BOGUS\"}|type"})
    @DisplayName("a node update with nothing to change or an invalid type answers 400, not 200/500")
    void nodeUpdateEmptyOrInvalid(String body, String messagePart) throws Exception {
        int id = node(workflow(), managementStatus(), "INITIAL");
        badRequest(send("put", "/api/v1/workflow-node/" + id, body), messagePart);
    }

    @Test
    @DisplayName("PUT /workflow-node/{id} with only a position still answers 200")
    void nodeUpdatePositionOnly() throws Exception {
        int id = node(workflow(), managementStatus(), "INITIAL");
        send("put", "/api/v1/workflow-node/" + id, "{\"positionX\":10.0,\"positionY\":20.0}")
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "POST /workflow-transition {0} answers 400 naming {1}")
    @CsvSource(delimiter = '|', value = {
        "{}|workflowDefinitionId",
        "{\"workflowDefinitionId\":WF}|originNodeId",
        "{\"workflowDefinitionId\":WF,\"originNodeId\":N1}|destinationNodeId"
    })
    @DisplayName("a transition create with a missing id answers 400, not 500")
    void transitionCreateMissingId(String body, String field) throws Exception {
        int wf = workflow();
        String json = body.replace("WF", String.valueOf(wf))
                .replace("N1", String.valueOf(node(wf, managementStatus(), "INITIAL")));
        badRequest(send("post", "/api/v1/workflow-transition", json), field);
    }

    @ParameterizedTest(name = "PUT /workflow-transition/'{id}' {0} answers 400 naming {1}")
    @CsvSource(delimiter = '|', value = {"{}|originNodeId", "{\"originNodeId\":N1}|destinationNodeId"})
    @DisplayName("a transition update with a missing node id answers 400, not 500")
    void transitionUpdateMissingId(String body, String field) throws Exception {
        int wf = workflow();
        int origin = node(wf, managementStatus(), "INITIAL");
        int destination = node(wf, managementStatus(), "FINAL");
        int id = created("/api/v1/workflow-transition", "{\"workflowDefinitionId\":" + wf
                + ",\"originNodeId\":" + origin + ",\"destinationNodeId\":" + destination + "}").get("id").asInt();
        badRequest(send("put", "/api/v1/workflow-transition/" + id, body.replace("N1", String.valueOf(origin))),
                field);
    }

    @ParameterizedTest(name = "contract of {0} documents 400 on POST and PUT")
    @CsvSource({"/api/v1/workflow-definition", "/api/v1/workflow-node", "/api/v1/workflow-transition"})
    @DisplayName("the contract documents 400 on create and update")
    void contractDocumentsBadRequest(String url) throws Exception {
        JsonNode spec = mapper.readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        assertThat(codes(spec, url, "post")).contains("400");
        assertThat(codes(spec, url + "/{id}", "put")).contains("400");
    }

    private static List<String> codes(JsonNode spec, String path, String method) {
        List<String> codes = new ArrayList<>();
        spec.path("paths").path(path).path(method).path("responses").fieldNames().forEachRemaining(codes::add);
        return codes;
    }
}
