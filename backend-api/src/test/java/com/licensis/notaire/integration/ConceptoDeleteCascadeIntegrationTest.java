package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.ConceptoRepository;
import com.licensis.notaire.repository.PlantillaPresupuestoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
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
class ConceptoDeleteCascadeIntegrationTest {

    @Autowired
    private ConceptoRepository conceptoRepository;
    @Autowired
    private PlantillaPresupuestoRepository plantillaRepository;
    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Test
    @Transactional
    @DisplayName("Should delete concepto and cascade-delete its plantilla_presupuesto children")
    void shouldDeleteConceptoWithCascadedChildren() {
        Integer idConcepto = createAndCommitConceptoWithPlantilla();

        conceptoRepository.deleteById(idConcepto);

        assertThat(conceptoRepository.existsById(idConcepto)).isFalse();
        assertThat(plantillaRepository.findByConceptoIdConcepto(idConcepto)).isEmpty();
    }

    private Integer createAndCommitConceptoWithPlantilla() {
        TipoDeTramite tipoDeTramite = new TipoDeTramite();
        tipoDeTramite.setNombre("Cascade Test Tramite");
        tipoDeTramite = tipoDeTramiteRepository.save(tipoDeTramite);

        Concepto concepto = new Concepto();
        concepto.setNombre("Cascade Test Concepto");
        concepto = conceptoRepository.save(concepto);

        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                tipoDeTramite.getIdTipoTramite(), concepto.getIdConcepto());
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(tipoDeTramite);
        plantilla.setConcepto(concepto);
        plantillaRepository.save(plantilla);

        Integer idConcepto = concepto.getIdConcepto();

        // Commit and start a fresh transaction so both rows above are genuinely
        // persisted and reloaded from scratch by the code under test, matching a real
        // HTTP request that arrives after they were created by an earlier request.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        return idConcepto;
    }
}
