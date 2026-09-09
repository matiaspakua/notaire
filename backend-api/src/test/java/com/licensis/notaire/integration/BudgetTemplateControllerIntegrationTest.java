package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ConceptRepository;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;
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

@RequirementCoverage({"CU39"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Presupuesto — cargar ítems desde la plantilla del type de trámite (CU39)")
class BudgetTemplateControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private BudgetTemplateRepository budgetTemplateRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson() throws Exception {
        String body = """
                {"firstName": "Client", "lastName": "Template IT", "identificationNumber": "%s",
                 "isClient": true, "identificationType": {"idIdentificationType": 1}}
                """.formatted("60" + (System.nanoTime() % 1000000));
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createBudget(Integer clientId) throws Exception {
        String body = """
                {"number": %d, "date": "2026-01-01", "encabezado": "Budget Template IT",
                 "status": "Pending", "amount": 1000.00, "person": {"personId": %d}}
                """.formatted((int) (System.nanoTime() % 100000), clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private ProcedureType createProcedureTypeConTemplate(String nameConcept, float value, int percentage) {
        ProcedureType procedureType = new ProcedureType();
        procedureType.setName("Tipo Tramite Plantilla IT " + System.nanoTime());
        procedureTypeRepository.save(procedureType);

        Concept concept = new Concept();
        concept.setName(nameConcept);
        concept.setValue(value);
        concept.setPercentage(percentage);
        conceptRepository.save(concept);

        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(
                new BudgetTemplatePK(procedureType.getIdProcedureType(), concept.getIdConcept()));
        template.setProcedureType(procedureType);
        template.setConcept(concept);
        budgetTemplateRepository.save(template);

        return procedureType;
    }

    @Test
    @DisplayName("Should load budget items from the type de trámite's plantilla")
    void shouldLoadItemsFromTemplate() throws Exception {
        Integer clientId = createPerson();
        Integer budgetId = createBudget(clientId);
        ProcedureType procedureType = createProcedureTypeConTemplate("Honorarios IT", 1500f, 10);

        mockMvc.perform(post("/api/v1/presupuestos/" + budgetId + "/items-desde-plantilla")
                        .param("tipoTramiteId", procedureType.getIdProcedureType().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Honorarios IT"))
                .andExpect(jsonPath("$[0].value").value(1500.0));
    }

    @Test
    @DisplayName("Should return 400 when the type de trámite has no plantilla configured")
    void shouldRejectWhenNoTemplateConfigured() throws Exception {
        Integer clientId = createPerson();
        Integer budgetId = createBudget(clientId);

        ProcedureType typeSinTemplate = new ProcedureType();
        typeSinTemplate.setName("Tipo Sin Plantilla IT " + System.nanoTime());
        procedureTypeRepository.save(typeSinTemplate);

        mockMvc.perform(post("/api/v1/presupuestos/" + budgetId + "/items-desde-plantilla")
                        .param("tipoTramiteId", typeSinTemplate.getIdProcedureType().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when the budget does not exist")
    void shouldReturnNotFoundForUnknownBudget() throws Exception {
        ProcedureType procedureType = createProcedureTypeConTemplate("Sellado IT", 500f, 0);

        mockMvc.perform(post("/api/v1/presupuestos/999999/items-desde-plantilla")
                        .param("tipoTramiteId", procedureType.getIdProcedureType().toString()))
                .andExpect(status().isNotFound());
    }
}
