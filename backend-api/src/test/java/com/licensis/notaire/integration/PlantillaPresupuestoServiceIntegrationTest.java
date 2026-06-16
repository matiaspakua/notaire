package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.ConceptoRepository;
import com.licensis.notaire.repository.PlantillaPresupuestoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PlantillaPresupuesto Service Integration Tests")
class PlantillaPresupuestoServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PlantillaPresupuestoRepository plantillaRepository;

    @Autowired
    private TipoDeTramiteRepository tipoTramiteRepository;

    @Autowired
    private ConceptoRepository conceptoRepository;

    private TipoDeTramite testTipoTramite;
    private Concepto testConcepto;

    @BeforeEach
    void setUp() {
        testTipoTramite = new TipoDeTramite();
        testTipoTramite.setNombre("Escritura de Compraventa");
        tipoTramiteRepository.save(testTipoTramite);

        testConcepto = new Concepto();
        testConcepto.setNombre("Honorarios");
        conceptoRepository.save(testConcepto);
    }

    @Test
    @DisplayName("Should create plantilla with all fields")
    void shouldCreatePlantillaWithAllFields() {
        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(testTipoTramite);
        plantilla.setConcepto(testConcepto);
        plantilla.setObservaciones("Test plantilla");

        PlantillaPresupuesto saved = plantillaRepository.save(plantilla);

        assertThat(saved).isNotNull();
        assertThat(saved.getPlantillaPresupuestoPK()).isEqualTo(pk);
        assertThat(saved.getObservaciones()).isEqualTo("Test plantilla");
    }

    @Test
    @DisplayName("Should retrieve plantilla by composite key")
    void shouldRetrievePlantillaByCompositeKey() {
        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(testTipoTramite);
        plantilla.setConcepto(testConcepto);
        plantilla.setObservaciones("Retrievable plantilla");
        plantillaRepository.save(plantilla);

        List<PlantillaPresupuesto> found = plantillaRepository
                .findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());

        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getObservaciones()).isEqualTo("Retrievable plantilla");
    }

    @Test
    @DisplayName("Should update plantilla observaciones")
    void shouldUpdatePlantillaObservaciones() {
        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(testTipoTramite);
        plantilla.setConcepto(testConcepto);
        plantilla.setObservaciones("Original");
        PlantillaPresupuesto saved = plantillaRepository.save(plantilla);

        saved.setObservaciones("Updated observaciones");
        plantillaRepository.save(saved);

        List<PlantillaPresupuesto> updated = plantillaRepository
                .findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());
        assertThat(updated.get(0).getObservaciones()).isEqualTo("Updated observaciones");
    }

    @Test
    @DisplayName("Should support null observaciones")
    void shouldSupportNullObservaciones() {
        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(testTipoTramite);
        plantilla.setConcepto(testConcepto);
        plantilla.setObservaciones(null);

        PlantillaPresupuesto saved = plantillaRepository.save(plantilla);

        assertThat(saved.getObservaciones()).isNull();
    }

    @Test
    @DisplayName("Should maintain relationships to related entities")
    void shouldMaintainRelationships() {
        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(testTipoTramite);
        plantilla.setConcepto(testConcepto);
        plantillaRepository.save(plantilla);

        List<PlantillaPresupuesto> found = plantillaRepository.findAll();
        PlantillaPresupuesto retrieved = found.stream()
                .filter(p -> p.getPlantillaPresupuestoPK().equals(pk))
                .findFirst()
                .orElseThrow();

        assertThat(retrieved.getTipoDeTramite()).isNotNull();
        assertThat(retrieved.getTipoDeTramite().getIdTipoTramite())
                .isEqualTo(testTipoTramite.getIdTipoTramite());
        assertThat(retrieved.getConcepto()).isNotNull();
        assertThat(retrieved.getConcepto().getIdConcepto())
                .isEqualTo(testConcepto.getIdConcepto());
    }

    @Test
    @DisplayName("Should handle multiple plantillas for different concepts")
    void shouldHandleMultiplePlantillasForDifferentConcepts() {
        Concepto concepto2 = new Concepto();
        concepto2.setNombre("Gastos");
        conceptoRepository.save(concepto2);

        PlantillaPresupuestoPK pk1 = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        PlantillaPresupuesto plantilla1 = new PlantillaPresupuesto();
        plantilla1.setPlantillaPresupuestoPK(pk1);
        plantilla1.setTipoDeTramite(testTipoTramite);
        plantilla1.setConcepto(testConcepto);
        plantilla1.setObservaciones("Plantilla 1");
        plantillaRepository.save(plantilla1);

        PlantillaPresupuestoPK pk2 = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                concepto2.getIdConcepto()
        );
        PlantillaPresupuesto plantilla2 = new PlantillaPresupuesto();
        plantilla2.setPlantillaPresupuestoPK(pk2);
        plantilla2.setTipoDeTramite(testTipoTramite);
        plantilla2.setConcepto(concepto2);
        plantilla2.setObservaciones("Plantilla 2");
        plantillaRepository.save(plantilla2);

        List<PlantillaPresupuesto> allForTipo = plantillaRepository
                .findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());

        assertThat(allForTipo).hasSize(2);
        assertThat(allForTipo).anySatisfy(p -> assertThat(p.getObservaciones()).isEqualTo("Plantilla 1"));
        assertThat(allForTipo).anySatisfy(p -> assertThat(p.getObservaciones()).isEqualTo("Plantilla 2"));
    }

    @Test
    @DisplayName("Should support delete operations")
    void shouldSupportDelete() {
        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(testTipoTramite);
        plantilla.setConcepto(testConcepto);
        plantillaRepository.save(plantilla);

        plantillaRepository.delete(plantilla);

        List<PlantillaPresupuesto> after = plantillaRepository
                .findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());
        assertThat(after).isEmpty();
    }

    @Test
    @DisplayName("Should handle bulk operations")
    void shouldHandleBulkOperations() {
        TipoDeTramite tipo2 = new TipoDeTramite();
        tipo2.setNombre("Poder");
        tipoTramiteRepository.save(tipo2);

        for (int i = 0; i < 3; i++) {
            Concepto concepto = new Concepto();
            concepto.setNombre("Concepto " + i);
            conceptoRepository.save(concepto);

            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                    tipo2.getIdTipoTramite(),
                    concepto.getIdConcepto()
            );
            PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
            plantilla.setPlantillaPresupuestoPK(pk);
            plantilla.setTipoDeTramite(tipo2);
            plantilla.setConcepto(concepto);
            plantillaRepository.save(plantilla);
        }

        List<PlantillaPresupuesto> allForTipo = plantillaRepository
                .findByTipoDeTramiteIdTipoTramite(tipo2.getIdTipoTramite());

        assertThat(allForTipo).hasSize(3);
    }
}
