package com.licensis.notaire.integration;

import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ConceptRepository;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@code Concepto}'s
 * {@code @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)} collection of
 * {@link PlantillaPresupuesto} — the highest-risk cascade shape identified in design.md
 * "Riesgos" for reverting a delete via Hibernate re-managing the loaded, cascaded
 * children in the same persistence context (#957).
 *
 * <p>Follows the same two-transaction shape as {@link HistorialDeleteIntegrationTest}:
 * the parent and child are created and committed in a prior transaction, matching a real
 * HTTP delete request that arrives after both rows already exist.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
class ConceptDeleteCascadeIntegrationTest {

    @Autowired
    private ConceptRepository conceptRepository;
    @Autowired
    private BudgetTemplateRepository templateRepository;
    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Test
    @Transactional
    @DisplayName("Should delete concepto and cascade-delete its plantilla_presupuesto children")
    void shouldDeleteConceptWithCascadedChildren() {
        Integer idConcept = createAndCommitConceptWithTemplate();

        conceptRepository.deleteById(idConcept);

        assertThat(conceptRepository.existsById(idConcept)).isFalse();
        assertThat(templateRepository.findByConceptIdConcept(idConcept)).isEmpty();
    }

    private Integer createAndCommitConceptWithTemplate() {
        ProcedureType procedureType = new ProcedureType();
        procedureType.setName("Cascade Test Tramite");
        procedureType = procedureTypeRepository.save(procedureType);

        Concept concept = new Concept();
        concept.setName("Cascade Test Concepto");
        concept = conceptRepository.save(concept);

        BudgetTemplatePK pk = new BudgetTemplatePK(
                procedureType.getIdProcedureType(), concept.getIdConcept());
        BudgetTemplate template = new BudgetTemplate();
        template.setBudgetTemplatePK(pk);
        template.setProcedureType(procedureType);
        template.setConcept(concept);
        templateRepository.save(template);

        Integer idConcept = concept.getIdConcept();

        // Commit and start a fresh transaction so both rows above are genuinely
        // persisted and reloaded from scratch by the code under test, matching a real
        // HTTP request that arrives after they were created by an earlier request.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        return idConcept;
    }
}
