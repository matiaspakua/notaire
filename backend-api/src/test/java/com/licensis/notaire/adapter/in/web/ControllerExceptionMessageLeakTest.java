package com.licensis.notaire.adapter.in.web;

import com.licensis.notaire.adapter.in.web.concept.ConceptController;
import com.licensis.notaire.adapter.in.web.document.DocumentTypeController;
import com.licensis.notaire.adapter.in.web.folio.FolioTypeController;
import com.licensis.notaire.adapter.in.web.workflow.WorkflowDefinitionController;
import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase;
import com.licensis.notaire.application.port.in.document.QueryDocumentTypesUseCase;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase;
import com.licensis.notaire.application.port.in.folio.QueryFolioTypesUseCase;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowDefinitionsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Issue #579 (slice 1): catch-all blocks in controllers returned {@code e.getMessage()} to the
 * client, leaking SQL, table/column names and "Failing row contains (...)" values (CWE-209).
 * Status codes stay as they were; only the body becomes the standard, safe ErrorResponse.
 */
class ControllerExceptionMessageLeakTest {

    private static final String SQL_LEAK = "could not execute statement [ERROR: null value in column \"name\" of "
            + "relation \"folio_types\" violates not-null constraint\n  Detail: Failing row contains (7, null, secret-notes)]";
    private static final String GENERIC_CONFLICT =
            "The request conflicts with existing data or violates a data constraint";
    private static final String GENERIC_SERVER_ERROR = "An unexpected error occurred";
    /** #579 slice 2: a constraint violation on a catalog create or update answers 400. */
    private static final String GENERIC_CONSTRAINT = "The submitted data violates a database constraint";

    private static void assertNoLeak(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("Failing row", "folio_types", "secret-notes", "could not execute");
    }

    @Test
    @DisplayName("POST /folio-types keeps 409 but no longer echoes the SQL error")
    void folioTypeCreate() throws Exception {
        ManageFolioTypesUseCase manage = mock(ManageFolioTypesUseCase.class);
        when(manage.create(any())).thenThrow(new DataIntegrityViolationException(SQL_LEAK));
        MockMvc mvc = standaloneSetup(new FolioTypeController(mock(QueryFolioTypesUseCase.class), manage)).build();

        MvcResult result = mvc.perform(post("/api/v1/folio-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(GENERIC_CONSTRAINT))
                .andReturn();
        assertNoLeak(result);
    }

    @Test
    @DisplayName("PUT /folio-types/{id} keeps 500 but no longer echoes the SQL error")
    void folioTypeUpdate() throws Exception {
        ManageFolioTypesUseCase manage = mock(ManageFolioTypesUseCase.class);
        doThrow(new DataIntegrityViolationException(SQL_LEAK)).when(manage).update(eq(1), any());
        MockMvc mvc = standaloneSetup(new FolioTypeController(mock(QueryFolioTypesUseCase.class), manage)).build();

        MvcResult result = mvc.perform(put("/api/v1/folio-types/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(GENERIC_CONSTRAINT))
                .andReturn();
        assertNoLeak(result);
    }

    @Test
    @DisplayName("PUT /document-types/{id} keeps 500 but no longer echoes the SQL error")
    void documentTypeUpdate() throws Exception {
        QueryDocumentTypesUseCase query = mock(QueryDocumentTypesUseCase.class);
        ManageDocumentTypesUseCase manage = mock(ManageDocumentTypesUseCase.class);
        when(query.getById(1)).thenReturn(DocumentType.restore(1, "ID", false, null, "", true, false, 0));
        doThrow(new DataIntegrityViolationException(SQL_LEAK)).when(manage).update(eq(1), any());
        MockMvc mvc = standaloneSetup(new DocumentTypeController(query, manage)).build();

        MvcResult result = mvc.perform(put("/api/v1/document-types/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(GENERIC_CONSTRAINT))
                .andReturn();
        assertNoLeak(result);
    }

    @Test
    @DisplayName("POST /workflow-definition answers 400 on a constraint and the error body carries no SQL text")
    void workflowDefinitionCreate() throws Exception {
        QueryWorkflowDefinitionsUseCase query = mock(QueryWorkflowDefinitionsUseCase.class);
        ManageWorkflowDefinitionsUseCase manage = mock(ManageWorkflowDefinitionsUseCase.class);
        when(manage.create(any())).thenThrow(new DataIntegrityViolationException(SQL_LEAK));
        MockMvc mvc = standaloneSetup(new WorkflowDefinitionController(query, manage)).build();

        MvcResult result = mvc.perform(post("/api/v1/workflow-definition").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(GENERIC_CONSTRAINT))
                .andReturn();
        assertNoLeak(result);
    }

    @Test
    @DisplayName("POST /concepts keeps an application-authored message for the user")
    void conceptCreateKeepsBusinessMessage() throws Exception {
        var manage = mock(com.licensis.notaire.application.port.in.concept.ManageConceptsUseCase.class);
        when(manage.create(any())).thenReturn(
                new com.licensis.notaire.application.port.out.concept.ConceptOpsPort.SaveResult.Failed(
                        new BusinessValidationException("Already exists a concept with that name"), true));
        MockMvc mvc = standaloneSetup(new ConceptController(
                        mock(com.licensis.notaire.application.port.in.concept.QueryConceptsUseCase.class), manage))
                .build();

        mvc.perform(post("/api/v1/concepts").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Already exists a concept with that name"));
    }

    /** Matches {@code catch (Exception e) { ... }} blocks (one nesting level is enough here). */
    private static final Pattern CATCH_ALL = Pattern.compile(
            "catch\\s*\\(\\s*(?:final\\s+)?Exception\\s+(\\w+)\\s*\\)\\s*\\{((?:[^{}]|\\{[^{}]*\\})*)\\}");

    @Test
    @DisplayName("no controller returns a caught Exception's message in a response body")
    void noControllerEchoesCaughtExceptionMessages() throws IOException {
        Path web = Path.of("src/main/java/com/licensis/notaire/adapter/in/web");
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(web)) {
            for (Path file : files.filter(p -> p.toString().endsWith("Controller.java")).toList()) {
                Matcher m = CATCH_ALL.matcher(Files.readString(file));
                while (m.find()) {
                    String var = Pattern.quote(m.group(1));
                    for (String line : m.group(2).split("\n")) {
                        boolean inResponse = line.contains("body(") || line.contains("Map.of(")
                                || line.contains("ResponseEntity");
                        if (inResponse && line.matches(".*\\b" + var + "\\.get(Localized)?Message\\(\\).*")) {
                            offenders.add(web.relativize(file) + ": " + line.strip());
                        }
                    }
                }
            }
        }
        assertThat(offenders).as("catch (Exception) blocks echoing exception text to clients").isEmpty();
    }
}
