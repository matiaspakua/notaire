package com.licensis.notaire.integration;

import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ConceptRepository;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BudgetTemplate Service Integration Tests")
class BudgetTemplateServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private BudgetTemplateRepository templateRepository;

    @Autowired
    private ProcedureTypeRepository typeProcedureRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    private ProcedureType testTypeProcedure;
    private Concept testConcept;

    @BeforeEach
    void setUp() {
        testTypeProcedure = new ProcedureType();
        testTypeProcedure.setName("Deed for Compraventa");
        typeProcedureRepository.save(testTypeProcedure);

        testConcept = new Concept();
        testConcept.setName("Honorarios");
        testConcept.setValue(java.math.BigDecimal.ZERO);
        testConcept.setPercentage(0);
        conceptRepository.save(testConcept);
    }

    @Test
    @DisplayName("Should create a budget template with all fields")
    void shouldCreateTemplateWithAllFields() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        template.setNotes("Test template");

        BudgetTemplate saved = templateRepository.save(template);

        assertThat(saved).isNotNull();
        assertThat(saved.getBudgetTemplatePK()).isEqualTo(pk);
        assertThat(saved.getNotes()).isEqualTo("Test template");
    }

    @Test
    @DisplayName("Should retrieve a budget template by composite key")
    void shouldRetrieveTemplateByCompositeKey() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        template.setNotes("Retrievable template");
        templateRepository.save(template);

        List<BudgetTemplate> found = templateRepository
                .findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());

        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getNotes()).isEqualTo("Retrievable template");
    }

    @Test
    @DisplayName("Should update budget-template notes")
    void shouldUpdateTemplateNotes() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        template.setNotes("Original");
        BudgetTemplate saved = templateRepository.save(template);

        saved.setNotes("Updated notes");
        templateRepository.save(saved);

        List<BudgetTemplate> updated = templateRepository
                .findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());
        assertThat(updated.get(0).getNotes()).isEqualTo("Updated notes");
    }

    @Test
    @DisplayName("Should support null notes")
    void shouldSupportNullNotes() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        template.setNotes(null);

        BudgetTemplate saved = templateRepository.save(template);

        assertThat(saved.getNotes()).isNull();
    }

    @Test
    @DisplayName("Should maintain relationships to related entities")
    void shouldMaintainRelationships() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        templateRepository.save(template);

        List<BudgetTemplate> found = templateRepository.findAll();
        BudgetTemplate retrieved = found.stream()
                .filter(p -> p.getBudgetTemplatePK().equals(pk))
                .findFirst()
                .orElseThrow();

        assertThat(retrieved.getProcedureType()).isNotNull();
        assertThat(retrieved.getProcedureType().getIdProcedureType())
                .isEqualTo(testTypeProcedure.getIdProcedureType());
        assertThat(retrieved.getConcept()).isNotNull();
        assertThat(retrieved.getConcept().getIdConcept())
                .isEqualTo(testConcept.getIdConcept());
    }

    @Test
    @DisplayName("Should handle multiple budget templates for different concepts")
    void shouldHandleMultipleTemplatesForDifferentConcepts() {
        Concept concept2 = new Concept();
        concept2.setName("Expenses");
        concept2.setValue(java.math.BigDecimal.ZERO);
        concept2.setPercentage(0);
        conceptRepository.save(concept2);

        BudgetTemplatePK pk1 = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template1 = new BudgetTemplate();
        template1.setBudgetTemplatePK(pk1);
        template1.setProcedureType(testTypeProcedure);
        template1.setConcept(testConcept);
        template1.setNotes("Template 1");
        templateRepository.save(template1);

        BudgetTemplatePK pk2 = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                concept2.getIdConcept()
        );
        BudgetTemplate template2 = new BudgetTemplate();
        template2.setBudgetTemplatePK(pk2);
        template2.setProcedureType(testTypeProcedure);
        template2.setConcept(concept2);
        template2.setNotes("Template 2");
        templateRepository.save(template2);

        List<BudgetTemplate> allForType = templateRepository
                .findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());

        assertThat(allForType).hasSize(2);
        assertThat(allForType).anySatisfy(p -> assertThat(p.getNotes()).isEqualTo("Template 1"));
        assertThat(allForType).anySatisfy(p -> assertThat(p.getNotes()).isEqualTo("Template 2"));
    }

    @Test
    @DisplayName("Should support delete operations")
    void shouldSupportDelete() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        templateRepository.save(template);

        templateRepository.delete(template);

        List<BudgetTemplate> after = templateRepository
                .findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());
        assertThat(after).isEmpty();
    }

    @Test
    @DisplayName("Should handle bulk operations")
    void shouldHandleBulkOperations() {
        ProcedureType type2 = new ProcedureType();
        type2.setName("Poder");
        typeProcedureRepository.save(type2);

        for (int i = 0; i < 3; i++) {
            Concept concept = new Concept();
            concept.setName("Concept " + i);
            concept.setValue(java.math.BigDecimal.ZERO);
            concept.setPercentage(0);
            conceptRepository.save(concept);

            BudgetTemplatePK pk = new BudgetTemplatePK(
                    type2.getIdProcedureType(),
                    concept.getIdConcept()
            );
            BudgetTemplate template = new BudgetTemplate();
            template.setBudgetTemplatePK(pk);
            template.setProcedureType(type2);
            template.setConcept(concept);
            templateRepository.save(template);
        }

        List<BudgetTemplate> allForType = templateRepository
                .findByProcedureTypeIdProcedureType(type2.getIdProcedureType());

        assertThat(allForType).hasSize(3);
    }
}
