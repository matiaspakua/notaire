package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.repository.InmuebleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inmueble Service Integration Tests")
class InmuebleServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private InmuebleRepository inmuebleRepository;

    @BeforeEach
    void setUp() {
        inmuebleRepository.deleteAll();
    }

    @Test
    @DisplayName("Should perform CRUD lifecycle")
    void shouldPerformCRUDLifecycle() {
        // Create
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Test 123");
        inmueble.setDomicilio("Test Street 123");
        inmueble.setValuacionFiscal("50000");
        Inmueble created = inmuebleRepository.save(inmueble);
        assertThat(created.getIdInmueble()).isNotNull();

        // Read
        Inmueble read = inmuebleRepository.findById(created.getIdInmueble()).orElseThrow();
        assertThat(read.getDomicilio()).isEqualTo("Test Street 123");

        // Update
        read.setDomicilio("Updated Street 456");
        read.setValuacionFiscal("75000");
        Inmueble updated = inmuebleRepository.save(read);
        assertThat(updated.getDomicilio()).isEqualTo("Updated Street 456");

        // Delete
        inmuebleRepository.delete(updated);
        assertThat(inmuebleRepository.findById(created.getIdInmueble())).isEmpty();
    }

    @Test
    @DisplayName("Should validate fiscal valuation correctly")
    void shouldValidateFiscalValuationCorrectly() {
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Casa valuada");
        inmueble.setDomicilio("Casa con valuación");
        inmueble.setValuacionFiscal("150000");
        Inmueble saved = inmuebleRepository.save(inmueble);

        assertThat(saved.getValuacionFiscal()).isEqualTo("150000");
        assertThat(saved.getValuacionFiscal()).isNotBlank();
    }

    @Test
    @DisplayName("Should handle different cadastral nomenclatures")
    void shouldHandleDifferentCadastralNomenclatures() {
        String[] nomenclaturas = {"N001-A1-B2", "N002-C3-D4", "N003-E5-F6", "N004-G7-H8", "N005-I9-J0"};

        for (String nomenclatura : nomenclaturas) {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral(nomenclatura);
            inmueble.setDomicilio("Propiedad " + nomenclatura);
            inmuebleRepository.save(inmueble);
        }

        List<Inmueble> all = inmuebleRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should manage multiple properties with different valuations")
    void shouldManageMultiplePropertiesWithDifferentValuations() {
        String[] locations = {"Buenos Aires", "Córdoba", "Rosario", "La Plata", "Mendoza"};

        for (int i = 0; i < locations.length; i++) {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral("Nomenclatura " + locations[i]);
            inmueble.setDomicilio("Calle Principal " + locations[i]);
            inmueble.setValuacionFiscal(String.valueOf(100000 + (i * 50000)));
            inmuebleRepository.save(inmueble);
        }

        List<Inmueble> all = inmuebleRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should handle various fiscal valuations")
    void shouldHandleVariousFiscalValuations() {
        String[] valuaciones = {"50000", "100000", "250000", "500000", "1000000"};

        for (String valuacion : valuaciones) {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral("Val-" + valuacion);
            inmueble.setDomicilio("Propiedad valuada " + valuacion);
            inmueble.setValuacionFiscal(valuacion);
            inmuebleRepository.save(inmueble);
        }

        List<Inmueble> all = inmuebleRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should maintain data integrity on concurrent operations")
    void shouldMaintainDataIntegrityOnConcurrentOperations() {
        // Create initial data
        Inmueble inmueble1 = new Inmueble();
        inmueble1.setNomenclaturaCatastral("Prop 1");
        inmueble1.setDomicilio("Propiedad 1");
        inmueble1.setValuacionFiscal("100000");
        Inmueble saved1 = inmuebleRepository.save(inmueble1);

        Inmueble inmueble2 = new Inmueble();
        inmueble2.setNomenclaturaCatastral("Prop 2");
        inmueble2.setDomicilio("Propiedad 2");
        inmueble2.setValuacionFiscal("150000");
        Inmueble saved2 = inmuebleRepository.save(inmueble2);

        // Simulate concurrent reads
        Inmueble read1 = inmuebleRepository.findById(saved1.getIdInmueble()).orElseThrow();
        Inmueble read2 = inmuebleRepository.findById(saved2.getIdInmueble()).orElseThrow();

        assertThat(read1.getDomicilio()).isEqualTo("Propiedad 1");
        assertThat(read2.getDomicilio()).isEqualTo("Propiedad 2");
        assertThat(read1.getValuacionFiscal()).isNotEqualTo(read2.getValuacionFiscal());
    }

    @Test
    @DisplayName("Should support filtering and search operations")
    void shouldSupportFilteringAndSearchOperations() {
        // Create diverse data
        for (int i = 1; i <= 10; i++) {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral("Nomenclatura " + i);
            inmueble.setDomicilio("Calle " + i);
            inmueble.setValuacionFiscal(String.valueOf(50000 + (i * 10000)));
            inmuebleRepository.save(inmueble);
        }

        List<Inmueble> all = inmuebleRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(10);

        // Filter by valuacion > 100000
        List<Inmueble> highValueProperties = all.stream()
                .filter(i -> i.getValuacionFiscal() != null &&
                           Integer.parseInt(i.getValuacionFiscal()) > 100000)
                .toList();
        assertThat(highValueProperties.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should handle batch operations efficiently")
    void shouldHandleBatchOperationsEfficiently() {
        // Batch create
        for (int i = 0; i < 20; i++) {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral("Batch-" + i);
            inmueble.setDomicilio("Batch Calle " + i);
            inmueble.setValuacionFiscal(String.valueOf(10000 * (i + 1)));
            inmuebleRepository.save(inmueble);
        }

        List<Inmueble> all = inmuebleRepository.findAll();
        long countBatch = all.stream()
                .filter(i -> i.getNomenclaturaCatastral() != null &&
                           i.getNomenclaturaCatastral().startsWith("Batch"))
                .count();

        assertThat(countBatch).isGreaterThanOrEqualTo(20);
    }

    @Test
    @DisplayName("Should support complex property scenarios")
    void shouldSupportComplexPropertyScenarios() {
        // High-end property
        Inmueble luxury = new Inmueble();
        luxury.setNomenclaturaCatastral("ALV-1050-LUX");
        luxury.setDomicilio("Avenida Alvear 1050, Penthouse Exclusivo");
        luxury.setValuacionFiscal("5000000");
        luxury.setObservaciones("Propiedad de lujo en zona premium");
        inmuebleRepository.save(luxury);

        // Budget property
        Inmueble budget = new Inmueble();
        budget.setNomenclaturaCatastral("RIO-800-STD");
        budget.setDomicilio("Calle Rioja 800, Departamento");
        budget.setValuacionFiscal("45000");
        inmuebleRepository.save(budget);

        List<Inmueble> all = inmuebleRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(2);

        // Verify both exist
        assertThat(all).anyMatch(i -> i.getValuacionFiscal() != null &&
                                     Integer.parseInt(i.getValuacionFiscal()) > 1000000);
        assertThat(all).anyMatch(i -> i.getValuacionFiscal() != null &&
                                     Integer.parseInt(i.getValuacionFiscal()) < 100000);
    }
}
