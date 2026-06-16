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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PlantillaPresupuesto Repository Integration Tests")
class PlantillaPresupuestoRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PlantillaPresupuestoRepository plantillaRepository;

    @Autowired
    private TipoDeTramiteRepository tipoTramiteRepository;

    @Autowired
    private ConceptoRepository conceptoRepository;

    private TipoDeTramite testTipoTramite;
    private Concepto testConcepto;
    private PlantillaPresupuesto testPlantilla;

    @BeforeEach
    void setUp() {
        testTipoTramite = new TipoDeTramite();
        testTipoTramite.setNombre("Escritura de Compraventa");
        tipoTramiteRepository.save(testTipoTramite);

        testConcepto = new Concepto();
        testConcepto.setNombre("Honorarios");
        conceptoRepository.save(testConcepto);

        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                testConcepto.getIdConcepto()
        );
        testPlantilla = new PlantillaPresupuesto();
        testPlantilla.setPlantillaPresupuestoPK(pk);
        testPlantilla.setTipoDeTramite(testTipoTramite);
        testPlantilla.setConcepto(testConcepto);
        testPlantilla.setObservaciones("Plantilla estándar");
        plantillaRepository.save(testPlantilla);
    }

    @Test
    @DisplayName("Should persist plantilla through repository")
    void shouldPersistPlantilla() {
        assertThat(testPlantilla.getPlantillaPresupuestoPK()).isNotNull();
        assertThat(plantillaRepository.findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite())).isNotEmpty();
    }

    @Test
    @DisplayName("Should find plantilla by tipo tramite")
    void shouldFindPlantillaByTipoTramite() {
        List<PlantillaPresupuesto> found = plantillaRepository.findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getObservaciones().equals("Plantilla estándar"));
    }

    @Test
    @DisplayName("Should return empty for non-existent tipo tramite")
    void shouldReturnEmptyForNonExistentTipoTramite() {
        List<PlantillaPresupuesto> found = plantillaRepository.findByTipoDeTramiteIdTipoTramite(999999);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all plantillas")
    void shouldFindAllPlantillas() {
        List<PlantillaPresupuesto> all = plantillaRepository.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getPlantillaPresupuestoPK().equals(testPlantilla.getPlantillaPresupuestoPK()));
    }

    @Test
    @DisplayName("Should update plantilla observaciones")
    void shouldUpdatePlantilla() {
        testPlantilla.setObservaciones("Observaciones actualizadas");
        PlantillaPresupuesto updated = plantillaRepository.save(testPlantilla);

        assertThat(updated.getObservaciones()).isEqualTo("Observaciones actualizadas");

        List<PlantillaPresupuesto> fetched = plantillaRepository.findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());
        assertThat(fetched).isNotEmpty()
                .anyMatch(p -> p.getObservaciones().equals("Observaciones actualizadas"));
    }

    @Test
    @DisplayName("Should delete plantilla")
    void shouldDeletePlantilla() {
        Long initialCount = (long) plantillaRepository.findAll().size();
        plantillaRepository.delete(testPlantilla);

        Long finalCount = (long) plantillaRepository.findAll().size();
        assertThat(finalCount).isLessThan(initialCount);
    }

    @Test
    @DisplayName("Should create plantilla with null observaciones")
    void shouldCreatePlantillaWithNullObservaciones() {
        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Poder");
        tipoTramiteRepository.save(tipo);

        Concepto concepto = new Concepto();
        concepto.setNombre("Gestión");
        conceptoRepository.save(concepto);

        PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(tipo.getIdTipoTramite(), concepto.getIdConcepto());
        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(pk);
        plantilla.setTipoDeTramite(tipo);
        plantilla.setConcepto(concepto);
        plantilla.setObservaciones(null);

        PlantillaPresupuesto saved = plantillaRepository.save(plantilla);
        assertThat(saved).isNotNull();

        List<PlantillaPresupuesto> found = plantillaRepository.findByTipoDeTramiteIdTipoTramite(tipo.getIdTipoTramite());
        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getObservaciones() == null);
    }

    @Test
    @DisplayName("Should handle multiple plantillas for same tipo tramite")
    void shouldHandleMultiplePlantillas() {
        Concepto concepto2 = new Concepto();
        concepto2.setNombre("Gastos");
        conceptoRepository.save(concepto2);

        PlantillaPresupuestoPK pk2 = new PlantillaPresupuestoPK(
                testTipoTramite.getIdTipoTramite(),
                concepto2.getIdConcepto()
        );
        PlantillaPresupuesto plantilla2 = new PlantillaPresupuesto();
        plantilla2.setPlantillaPresupuestoPK(pk2);
        plantilla2.setTipoDeTramite(testTipoTramite);
        plantilla2.setConcepto(concepto2);
        plantilla2.setObservaciones("Segunda plantilla");

        plantillaRepository.save(plantilla2);

        List<PlantillaPresupuesto> all = plantillaRepository.findAll();
        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getPlantillaPresupuestoPK().equals(testPlantilla.getPlantillaPresupuestoPK()))
                .anyMatch(p -> p.getPlantillaPresupuestoPK().equals(plantilla2.getPlantillaPresupuestoPK()));
    }

    @Test
    @DisplayName("Should update plantilla fields independently")
    void shouldUpdateFieldsIndependently() {
        testPlantilla.setObservaciones("Updated observaciones 1");
        plantillaRepository.save(testPlantilla);

        testPlantilla.setObservaciones("Updated observaciones 2");
        plantillaRepository.save(testPlantilla);

        List<PlantillaPresupuesto> fetched = plantillaRepository.findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());
        assertThat(fetched).isNotEmpty()
                .anyMatch(p -> p.getObservaciones().equals("Updated observaciones 2"));
    }

    @Test
    @DisplayName("Should handle transaction consistency")
    void shouldMaintainTransactionConsistency() {
        testPlantilla.setObservaciones("Antes de eliminar");
        plantillaRepository.save(testPlantilla);

        plantillaRepository.delete(testPlantilla);
        List<PlantillaPresupuesto> found = plantillaRepository.findByTipoDeTramiteIdTipoTramite(testTipoTramite.getIdTipoTramite());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should create distinct plantillas with different IDs")
    void shouldCreateDistinctPlantillas() {
        TipoDeTramite tipo1 = new TipoDeTramite();
        tipo1.setNombre("Tipo 1");
        tipoTramiteRepository.save(tipo1);

        TipoDeTramite tipo2 = new TipoDeTramite();
        tipo2.setNombre("Tipo 2");
        tipoTramiteRepository.save(tipo2);

        Concepto concepto1 = new Concepto();
        concepto1.setNombre("Concepto 1");
        conceptoRepository.save(concepto1);

        Concepto concepto2 = new Concepto();
        concepto2.setNombre("Concepto 2");
        conceptoRepository.save(concepto2);

        PlantillaPresupuestoPK pk1 = new PlantillaPresupuestoPK(tipo1.getIdTipoTramite(), concepto1.getIdConcepto());
        PlantillaPresupuesto plantilla1 = new PlantillaPresupuesto();
        plantilla1.setPlantillaPresupuestoPK(pk1);
        plantilla1.setTipoDeTramite(tipo1);
        plantilla1.setConcepto(concepto1);
        plantilla1.setObservaciones("Plantilla 1");
        plantillaRepository.save(plantilla1);

        PlantillaPresupuestoPK pk2 = new PlantillaPresupuestoPK(tipo2.getIdTipoTramite(), concepto2.getIdConcepto());
        PlantillaPresupuesto plantilla2 = new PlantillaPresupuesto();
        plantilla2.setPlantillaPresupuestoPK(pk2);
        plantilla2.setTipoDeTramite(tipo2);
        plantilla2.setConcepto(concepto2);
        plantilla2.setObservaciones("Plantilla 2");
        plantillaRepository.save(plantilla2);

        List<PlantillaPresupuesto> found1 = plantillaRepository.findByTipoDeTramiteIdTipoTramite(tipo1.getIdTipoTramite());
        List<PlantillaPresupuesto> found2 = plantillaRepository.findByTipoDeTramiteIdTipoTramite(tipo2.getIdTipoTramite());

        assertThat(found1).isNotEmpty().anyMatch(p -> p.getObservaciones().equals("Plantilla 1"));
        assertThat(found2).isNotEmpty().anyMatch(p -> p.getObservaciones().equals("Plantilla 2"));
    }
}
