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
    void shouldFindAllPlantillas() {
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
    void shouldHandleMultiplePlantillas() {
        Concept concepto2 = new Concept();
        concepto2.setName("Gastos");
        concepto2.setValue(java.math.BigDecimal.ZERO);
        concepto2.setPercentage(0);
        conceptRepository.save(concepto2);

        BudgetTemplatePK pk2 = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                concepto2.getIdConcept()
        );
        BudgetTemplate plantilla2 = new BudgetTemplate();
        plantilla2.setBudgetTemplatePK(pk2);
        plantilla2.setProcedureType(testTypeProcedure);
        plantilla2.setConcept(concepto2);
        plantilla2.setNotes("Segunda plantilla");

        templateRepository.save(plantilla2);

        List<BudgetTemplate> all = templateRepository.findAll();
        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getBudgetTemplatePK().equals(testTemplate.getBudgetTemplatePK()))
                .anyMatch(p -> p.getBudgetTemplatePK().equals(plantilla2.getBudgetTemplatePK()));
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
    void shouldCreateDistinctPlantillas() {
        ProcedureType tipo1 = new ProcedureType();
        tipo1.setName("Tipo 1");
        typeProcedureRepository.save(tipo1);

        ProcedureType tipo2 = new ProcedureType();
        tipo2.setName("Tipo 2");
        typeProcedureRepository.save(tipo2);

        Concept concepto1 = new Concept();
        concepto1.setName("Concept 1");
        concepto1.setValue(java.math.BigDecimal.ZERO);
        concepto1.setPercentage(0);
        conceptRepository.save(concepto1);

        Concept concepto2 = new Concept();
        concepto2.setName("Concept 2");
        concepto2.setValue(java.math.BigDecimal.ZERO);
        concepto2.setPercentage(0);
        conceptRepository.save(concepto2);

        BudgetTemplatePK pk1 = new BudgetTemplatePK(tipo1.getIdProcedureType(), concepto1.getIdConcept());
        BudgetTemplate plantilla1 = new BudgetTemplate();
        plantilla1.setBudgetTemplatePK(pk1);
        plantilla1.setProcedureType(tipo1);
        plantilla1.setConcept(concepto1);
        plantilla1.setNotes("Plantilla 1");
        templateRepository.save(plantilla1);

        BudgetTemplatePK pk2 = new BudgetTemplatePK(tipo2.getIdProcedureType(), concepto2.getIdConcept());
        BudgetTemplate plantilla2 = new BudgetTemplate();
        plantilla2.setBudgetTemplatePK(pk2);
        plantilla2.setProcedureType(tipo2);
        plantilla2.setConcept(concepto2);
        plantilla2.setNotes("Plantilla 2");
        templateRepository.save(plantilla2);

        List<BudgetTemplate> found1 = templateRepository.findByProcedureTypeIdProcedureType(tipo1.getIdProcedureType());
        List<BudgetTemplate> found2 = templateRepository.findByProcedureTypeIdProcedureType(tipo2.getIdProcedureType());

        assertThat(found1).isNotEmpty().anyMatch(p -> p.getNotes().equals("Plantilla 1"));
        assertThat(found2).isNotEmpty().anyMatch(p -> p.getNotes().equals("Plantilla 2"));
    }
}
