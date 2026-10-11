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

@DisplayName("BudgetTemplate Repository Integration Tests")
class BudgetTemplateRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private BudgetTemplateRepository templateRepository;

    @Autowired
    private ProcedureTypeRepository typeProcedureRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    private ProcedureType testTypeProcedure;
    private Concept testConcept;
    private BudgetTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTypeProcedure = new ProcedureType();
        testTypeProcedure.setName("Deed de Compraventa");
        typeProcedureRepository.save(testTypeProcedure);

        testConcept = new Concept();
        testConcept.setName("Honorarios");
        testConcept.setValue(java.math.BigDecimal.ZERO);
        testConcept.setPercentage(0);
        conceptRepository.save(testConcept);

        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        testTemplate = new BudgetTemplate();
        testTemplate.setBudgetTemplatePK(pk);
        testTemplate.setProcedureType(testTypeProcedure);
        testTemplate.setConcept(testConcept);
        testTemplate.setNotes("Template estándar");
        templateRepository.save(testTemplate);
    }

    @Test
    @DisplayName("Should persist a budget template through the repository")
    void shouldPersistTemplate() {
        assertThat(testTemplate.getBudgetTemplatePK()).isNotNull();
        assertThat(templateRepository.findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType())).isNotEmpty();
    }

    @Test
    @DisplayName("Should find template by type procedure")
    void shouldFindTemplateByTypeProcedure() {
        List<BudgetTemplate> found = templateRepository.findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getNotes().equals("Template estándar"));
    }

    @Test
    @DisplayName("Should return empty for non-existent type procedure")
    void shouldReturnEmptyForNonExistentTypeProcedure() {
        List<BudgetTemplate> found = templateRepository.findByProcedureTypeIdProcedureType(999999);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all budget templates")
    void shouldFindAllTemplates() {
        List<BudgetTemplate> all = templateRepository.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getBudgetTemplatePK().equals(testTemplate.getBudgetTemplatePK()));
    }

    @Test
    @DisplayName("Should update budget-template notes")
    void shouldUpdateTemplate() {
        testTemplate.setNotes("Observaciones actualizadas");
        BudgetTemplate updated = templateRepository.save(testTemplate);

        assertThat(updated.getNotes()).isEqualTo("Observaciones actualizadas");

        List<BudgetTemplate> fetched = templateRepository.findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());
        assertThat(fetched).isNotEmpty()
                .anyMatch(p -> p.getNotes().equals("Observaciones actualizadas"));
    }

    @Test
    @DisplayName("Should delete the budget template")
    void shouldDeleteTemplate() {
        Long initialCount = (long) templateRepository.findAll().size();
        templateRepository.delete(testTemplate);

        Long finalCount = (long) templateRepository.findAll().size();
        assertThat(finalCount).isLessThan(initialCount);
    }

    @Test
    @DisplayName("Should create a budget template with null notes")
    void shouldCreateTemplateWithNullNotes() {
        ProcedureType type = new ProcedureType();
        type.setName("Poder");
        typeProcedureRepository.save(type);

        Concept concept = new Concept();
        concept.setName("Management");
        concept.setValue(java.math.BigDecimal.ZERO);
        concept.setPercentage(0);
        conceptRepository.save(concept);

        BudgetTemplatePK pk = new BudgetTemplatePK(type.getIdProcedureType(), concept.getIdConcept());
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(type);
        template.setConcept(concept);
        template.setNotes(null);

        BudgetTemplate saved = templateRepository.save(template);
        assertThat(saved).isNotNull();

        List<BudgetTemplate> found = templateRepository.findByProcedureTypeIdProcedureType(type.getIdProcedureType());
        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getNotes() == null);
    }

    @Test
    @DisplayName("Should handle multiple budget templates for the same procedure type")
    void shouldHandleMultipleTemplates() {
        Concept concept2 = new Concept();
        concept2.setName("Gastos");
        concept2.setValue(java.math.BigDecimal.ZERO);
        concept2.setPercentage(0);
        conceptRepository.save(concept2);

        BudgetTemplatePK pk2 = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                concept2.getIdConcept()
        );
        BudgetTemplate template2 = new BudgetTemplate();
        template2.setBudgetTemplatePK(pk2);
        template2.setProcedureType(testTypeProcedure);
        template2.setConcept(concept2);
        template2.setNotes("Second template");

        templateRepository.save(template2);

        List<BudgetTemplate> all = templateRepository.findAll();
        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getBudgetTemplatePK().equals(testTemplate.getBudgetTemplatePK()))
                .anyMatch(p -> p.getBudgetTemplatePK().equals(template2.getBudgetTemplatePK()));
    }

    @Test
    @DisplayName("Should update budget-template fields independently")
    void shouldUpdateFieldsIndependently() {
        testTemplate.setNotes("Updated notes 1");
        templateRepository.save(testTemplate);

        testTemplate.setNotes("Updated notes 2");
        templateRepository.save(testTemplate);

        List<BudgetTemplate> fetched = templateRepository.findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());
        assertThat(fetched).isNotEmpty()
                .anyMatch(p -> p.getNotes().equals("Updated notes 2"));
    }

    @Test
    @DisplayName("Should handle transaction consistency")
    void shouldMaintainTransactionConsistency() {
        testTemplate.setNotes("Antes of delete");
        templateRepository.save(testTemplate);

        templateRepository.delete(testTemplate);
        List<BudgetTemplate> found = templateRepository.findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should create distinct budget templates with different IDs")
    void shouldCreateDistinctTemplates() {
        ProcedureType type1 = new ProcedureType();
        type1.setName("Tipo 1");
        typeProcedureRepository.save(type1);

        ProcedureType type2 = new ProcedureType();
        type2.setName("Tipo 2");
        typeProcedureRepository.save(type2);

        Concept concept1 = new Concept();
        concept1.setName("Concept 1");
        concept1.setValue(java.math.BigDecimal.ZERO);
        concept1.setPercentage(0);
        conceptRepository.save(concept1);

        Concept concept2 = new Concept();
        concept2.setName("Concept 2");
        concept2.setValue(java.math.BigDecimal.ZERO);
        concept2.setPercentage(0);
        conceptRepository.save(concept2);

        BudgetTemplatePK pk1 = new BudgetTemplatePK(type1.getIdProcedureType(), concept1.getIdConcept());
        BudgetTemplate template1 = new BudgetTemplate();
        template1.setBudgetTemplatePK(pk1);
        template1.setProcedureType(type1);
        template1.setConcept(concept1);
        template1.setNotes("Template 1");
        templateRepository.save(template1);

        BudgetTemplatePK pk2 = new BudgetTemplatePK(type2.getIdProcedureType(), concept2.getIdConcept());
        BudgetTemplate template2 = new BudgetTemplate();
        template2.setBudgetTemplatePK(pk2);
        template2.setProcedureType(type2);
        template2.setConcept(concept2);
        template2.setNotes("Template 2");
        templateRepository.save(template2);

        List<BudgetTemplate> found1 = templateRepository.findByProcedureTypeIdProcedureType(type1.getIdProcedureType());
        List<BudgetTemplate> found2 = templateRepository.findByProcedureTypeIdProcedureType(type2.getIdProcedureType());

        assertThat(found1).isNotEmpty().anyMatch(p -> p.getNotes().equals("Template 1"));
        assertThat(found2).isNotEmpty().anyMatch(p -> p.getNotes().equals("Template 2"));
    }
}
