package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * CU01 - Preparar Budget, against the real Flyway-managed Postgres
 * schema. After #1068/#1124, create/update bind {@code BudgetRequest} (accepts
 * nested {@code person.personId} or flat {@code personId}) and return
 * {@code BudgetResponse} with nested {@code person} matching frontend
 * {@code DtoPerson} ({@code personId}, {@code name}, {@code lastName}) —
 * Issue #883 / PR #1132.
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("Budget - association with a person (CU01)")
class BudgetPersonAssociationPgIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Client IT", "lastName": "CU01", "identificationNumber": "%s",
                 "isClient": true, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    @Test
    @DisplayName("Should link the Budget to the client when created with the field the frontend sends")
    void shouldPersistPersonAssociationOnCreate() throws Exception {
        Integer personId = createPerson("883pg001");
        String body = """
                {"number": 883001, "date": "2026-01-01", "heading": "Budget CU01 pg",
                 "status": "BORRADOR", "person": {"personId": %d}}
                """.formatted(personId);

        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.person.personId").value(personId))
                .andExpect(jsonPath("$.person.name").value("Client IT"))
                .andExpect(jsonPath("$.person.lastName").value("CU01"));
    }

    @Test
    @DisplayName("Should link the Budget to the client when edited to add one")
    void shouldPersistPersonAssociationOnUpdate() throws Exception {
        Integer personId = createPerson("883pg002");
        String createBody = """
                {"number": 883002, "date": "2026-01-01", "heading": "Budget CU01 pg without client",
                 "status": "BORRADOR"}
                """;
        MvcResult createResult = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer budgetId = mapper.readTree(createResult.getResponse().getContentAsString())
                .get("idBudget").asInt();

        String updateBody = """
                {"number": 883002, "date": "2026-01-01", "heading": "Budget CU01 pg without client",
                 "status": "BORRADOR", "person": {"personId": %d}}
                """.formatted(personId);

        mockMvc.perform(put("/api/v1/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person.personId").value(personId))
                .andExpect(jsonPath("$.person.name").value("Client IT"))
                .andExpect(jsonPath("$.person.lastName").value("CU01"));
    }

    @Test
    @DisplayName("Should still create a Budget when the client association is omitted")
    void shouldCreateWithoutPersonWhenOmitted() throws Exception {
        String body = """
                {"number": 883003, "date": "2026-01-01", "heading": "Budget CU01 pg opcional",
                 "status": "BORRADOR"}
                """;

        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.person").doesNotExist());
    }

    @Test
    @DisplayName("Should return the client association when reading a single Budget")
    void shouldReturnPersonFieldOnGetById() throws Exception {
        Integer personId = createPerson("883pg004");
        String body = """
                {"number": 883004, "date": "2026-01-01", "heading": "Budget CU01 pg detail",
                 "status": "BORRADOR", "person": {"personId": %d}}
                """.formatted(personId);
        MvcResult createResult = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer budgetId = mapper.readTree(createResult.getResponse().getContentAsString())
                .get("idBudget").asInt();

        mockMvc.perform(get("/api/v1/budgets/" + budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person.personId").value(personId))
                .andExpect(jsonPath("$.person.name").value("Client IT"))
                .andExpect(jsonPath("$.person.lastName").value("CU01"));
    }

    @Test
    @DisplayName("Should reflect the presence or absence of the client association per record when listing")
    void shouldReflectPersonAcrossListedBudgets() throws Exception {
        Integer personId = createPerson("883pg005");
        String withPersonBody = """
                {"number": 883005, "date": "2026-01-01", "heading": "Budget CU01 pg with client",
                 "status": "BORRADOR", "person": {"personId": %d}}
                """.formatted(personId);
        String withoutPersonBody = """
                {"number": 883006, "date": "2026-01-01", "heading": "Budget CU01 pg without listed client",
                 "status": "BORRADOR"}
                """;

        MvcResult withPersonResult = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withPersonBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer withPersonId = mapper.readTree(withPersonResult.getResponse().getContentAsString())
                .get("idBudget").asInt();

        MvcResult withoutPersonResult = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withoutPersonBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer withoutPersonId = mapper.readTree(withoutPersonResult.getResponse().getContentAsString())
                .get("idBudget").asInt();

        mockMvc.perform(get("/api/v1/budgets/" + withPersonId))
                .andExpect(jsonPath("$.person.personId").value(personId))
                .andExpect(jsonPath("$.person.name").value("Client IT"))
                .andExpect(jsonPath("$.person.lastName").value("CU01"));
        mockMvc.perform(get("/api/v1/budgets/" + withoutPersonId))
                .andExpect(jsonPath("$.person").doesNotExist());
    }
}
