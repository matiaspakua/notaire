package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@code Tramite} has two bidirectional EAGER/LAZY relationships that Jackson recurses through
 * unless the back-reference side is excluded:
 * <ul>
 *   <li>{@code Procedure.fkIdBudget} (EAGER) &lt;-&gt; {@code Budget.procedureList} (LAZY)</li>
 *   <li>{@code Procedure.fkIdManagement} (EAGER) &lt;-&gt; {@code DeedManagement.procedureList} (LAZY)</li>
 * </ul>
 * Both previously 500'd GET /api/v1/tramites ("Failed to write request") as soon as production
 * data populated either relation — which V1 seed data does via {@code fk_id_gestion}.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("Tramite — list endpoint serializes without cyclic recursion")
class ProcedureSerializationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ProcedureRepository procedureRepository;
    @Autowired
    private DeedManagementRepository managementRepository;
    @Autowired
    private ManagementStatusRepository statusRepository;
    @Autowired
    private PersonRepository personRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createTypeProcedure() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tipo-tramite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Procedure Ciclo Budget", "isRegistered": false,
                                 "isArchived": false, "associatesProperties": false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProcedureType").asInt();
    }

    private Integer createBudget() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number": 1, "encabezado": "budget ciclo IT", "status": "Pending",
                                 "date": "2026-07-24"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private Integer createProcedureForBudget(Integer typeProcedureId, Integer budgetId) throws Exception {
        String body = """
                {"notes": "procedure ciclo IT", "fkIdProcedureType": {"idProcedureType": %d},
                 "fkIdBudget": {"idBudget": %d}}
                """.formatted(typeProcedureId, budgetId);
        MvcResult result = mockMvc.perform(post("/api/v1/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProcedure").asInt();
    }

    @Test
    @DisplayName("Should list procedures as valid JSON when a tramite references its budget")
    void shouldListProceduresWithoutCyclicRecursion() throws Exception {
        Integer typeProcedureId = createTypeProcedure();
        Integer budgetId = createBudget();
        Integer procedureId = createProcedureForBudget(typeProcedureId, budgetId);

        MvcResult result = mockMvc.perform(get("/api/v1/tramites"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = body.get("content");
        assertThat(content).as("Page response should have a content array").isNotNull();

        JsonNode procedure = null;
        for (JsonNode candidate : content) {
            if (candidate.get("idProcedure").asInt() == procedureId) {
                procedure = candidate;
                break;
            }
        }
        assertThat(procedure).as("created tramite should be in the list").isNotNull();

        JsonNode budget = procedure.get("fkIdBudget");
        assertThat(budget).as("tramite should embed its fkIdBudget").isNotNull();
        assertThat(budget.get("idBudget").asInt()).isEqualTo(budgetId);
        assertThat(budget.has("procedureList"))
                .as("budget must not embed its procedureList (cyclic reference)")
                .isFalse();
    }

    @Test
    @DisplayName("Should list procedures as valid JSON when a gestion references the tramite (matches V1 seed data)")
    void shouldListProceduresWithoutCyclicRecursionThroughManagement() throws Exception {
        Integer typeProcedureId = createTypeProcedure();
        ProcedureType type = new ProcedureType();
        type.setIdProcedureType(typeProcedureId);

        ManagementStatus status = new ManagementStatus();
        status.setName("Ciclo IT Estado");
        status = statusRepository.save(status);

        Person notary = personRepository.findAll().get(0);

        DeedManagement management = new DeedManagement();
        management.setIdManagement(null);
        management.setNumber(123456);
        management.setEncabezado("Gestion Ciclo IT");
        management.setDateStart(new Date());
        management.setFkIdManagementStatus(status);
        management.setFkIdNotaryPerson(notary);
        management = managementRepository.save(management);

        Procedure procedure = new Procedure();
        procedure.setIdProcedure(null);
        procedure.setFkIdManagement(management);
        procedure.setFkIdProcedureType(type);
        procedureRepository.save(procedure);

        MvcResult result = mockMvc.perform(get("/api/v1/tramites"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body2 = mapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = body2.get("content");
        assertThat(content).as("Page response should have a content array").isNotNull();

        JsonNode found = null;
        for (JsonNode candidate : content) {
            JsonNode managementNode = candidate.get("fkIdManagement");
            if (managementNode != null && !managementNode.isNull()
                    && managementNode.get("idManagement").asInt() == management.getIdManagement()) {
                found = candidate;
                break;
            }
        }
        assertThat(found).as("tramite linked to the gestion should be in the list").isNotNull();
        assertThat(found.get("fkIdManagement").has("procedureList"))
                .as("gestion must not embed its procedureList (cyclic reference)")
                .isFalse();
    }
}
