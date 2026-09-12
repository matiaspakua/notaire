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

@DisplayName("PlantillaPresupuesto Service Integration Tests")
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
        testTypeProcedure.setName("Escritura de Compraventa");
        typeProcedureRepository.save(testTypeProcedure);

        testConcept = new Concept();
        testConcept.setName("Honorarios");
        conceptRepository.save(testConcept);
    }

    @Test
    @DisplayName("Should create plantilla with all fields")
    void shouldCreateTemplateWithAllFields() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        template.setNotes("Test plantilla");

        BudgetTemplate saved = templateRepository.save(template);

        assertThat(saved).isNotNull();
        assertThat(saved.getBudgetTemplatePK()).isEqualTo(pk);
        assertThat(saved.getNotes()).isEqualTo("Test plantilla");
    }

    @Test
    @DisplayName("Should retrieve plantilla by composite key")
    void shouldRetrieveTemplateByCompositeKey() {
        BudgetTemplatePK pk = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(testTypeProcedure);
        template.setConcept(testConcept);
        template.setNotes("Retrievable plantilla");
        templateRepository.save(template);

        List<BudgetTemplate> found = templateRepository
                .findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());

        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getNotes()).isEqualTo("Retrievable plantilla");
    }

    @Test
    @DisplayName("Should update plantilla notes")
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
    @DisplayName("Should handle multiple plantillas for different concepts")
    void shouldHandleMultiplePlantillasForDifferentConcepts() {
        Concept concepto2 = new Concept();
        concepto2.setName("Gastos");
        conceptRepository.save(concepto2);

        BudgetTemplatePK pk1 = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                testConcept.getIdConcept()
        );
        BudgetTemplate plantilla1 = new BudgetTemplate();
        plantilla1.setBudgetTemplatePK(pk1);
        plantilla1.setProcedureType(testTypeProcedure);
        plantilla1.setConcept(testConcept);
        plantilla1.setNotes("Plantilla 1");
        templateRepository.save(plantilla1);

        BudgetTemplatePK pk2 = new BudgetTemplatePK(
                testTypeProcedure.getIdProcedureType(),
                concepto2.getIdConcept()
        );
        BudgetTemplate plantilla2 = new BudgetTemplate();
        plantilla2.setBudgetTemplatePK(pk2);
        plantilla2.setProcedureType(testTypeProcedure);
        plantilla2.setConcept(concepto2);
        plantilla2.setNotes("Plantilla 2");
        templateRepository.save(plantilla2);

        List<BudgetTemplate> allForType = templateRepository
                .findByProcedureTypeIdProcedureType(testTypeProcedure.getIdProcedureType());

        assertThat(allForType).hasSize(2);
        assertThat(allForType).anySatisfy(p -> assertThat(p.getNotes()).isEqualTo("Plantilla 1"));
        assertThat(allForType).anySatisfy(p -> assertThat(p.getNotes()).isEqualTo("Plantilla 2"));
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
        ProcedureType tipo2 = new ProcedureType();
        tipo2.setName("Poder");
        typeProcedureRepository.save(tipo2);

        for (int i = 0; i < 3; i++) {
            Concept concept = new Concept();
            concept.setName("Concepto " + i);
            conceptRepository.save(concept);

            BudgetTemplatePK pk = new BudgetTemplatePK(
                    tipo2.getIdProcedureType(),
                    concept.getIdConcept()
            );
            BudgetTemplate template = new BudgetTemplate();
            template.setBudgetTemplatePK(pk);
            template.setProcedureType(tipo2);
            template.setConcept(concept);
            templateRepository.save(template);
        }

        List<BudgetTemplate> allForType = templateRepository
                .findByProcedureTypeIdProcedureType(tipo2.getIdProcedureType());

        assertThat(allForType).hasSize(3);
    }
}
