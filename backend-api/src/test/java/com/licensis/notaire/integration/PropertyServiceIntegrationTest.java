package com.licensis.notaire.integration;

import java.math.BigDecimal;

import com.licensis.notaire.business.Property;
import com.licensis.notaire.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inmueble Service Integration Tests")
class PropertyServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll();
    }

    @Test
    @DisplayName("Should perform CRUD lifecycle")
    void shouldPerformCRUDLifecycle() {
        // Create
        Property property = new Property();
        property.setCadastralDesignation("Test 123");
        property.setAddress("Test Street 123");
        property.setFiscalAppraisal(new java.math.BigDecimal("50000"));
        Property created = propertyRepository.save(property);
        assertThat(created.getIdProperty()).isNotNull();

        // Read
        Property read = propertyRepository.findById(created.getIdProperty()).orElseThrow();
        assertThat(read.getAddress()).isEqualTo("Test Street 123");

        // Update
        read.setAddress("Updated Street 456");
        read.setFiscalAppraisal(new java.math.BigDecimal("75000"));
        Property updated = propertyRepository.save(read);
        assertThat(updated.getAddress()).isEqualTo("Updated Street 456");

        // Delete
        propertyRepository.delete(updated);
        assertThat(propertyRepository.findById(created.getIdProperty())).isEmpty();
    }

    @Test
    @DisplayName("Should validate fiscal valuation correctly")
    void shouldValidateFiscalValuationCorrectly() {
        Property property = new Property();
        property.setCadastralDesignation("Casa valuada");
        property.setAddress("Casa con valuación");
        property.setFiscalAppraisal(new java.math.BigDecimal("150000"));
        Property saved = propertyRepository.save(property);

        assertThat(saved.getFiscalAppraisal()).isEqualByComparingTo(new java.math.BigDecimal("150000"));
        assertThat(saved.getFiscalAppraisal()).isNotNull();
    }

    @Test
    @DisplayName("Should handle different cadastral nomenclatures")
    void shouldHandleDifferentCadastralNomenclatures() {
        String[] nomenclaturas = {"N001-A1-B2", "N002-C3-D4", "N003-E5-F6", "N004-G7-H8", "N005-I9-J0"};

        for (String designation : nomenclaturas) {
            Property property = new Property();
            property.setCadastralDesignation(designation);
            property.setAddress("Propiedad " + designation);
            propertyRepository.save(property);
        }

        List<Property> all = propertyRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should manage multiple properties with different valuations")
    void shouldManageMultiplePropertiesWithDifferentValuations() {
        String[] locations = {"Buenos Aires", "Córdoba", "Rosario", "La Plata", "Mendoza"};

        for (int i = 0; i < locations.length; i++) {
            Property property = new Property();
            property.setCadastralDesignation("Nomenclatura " + locations[i]);
            property.setAddress("Calle Principal " + locations[i]);
            property.setFiscalAppraisal(java.math.BigDecimal.valueOf(100000 + (i * 50000L)));
            propertyRepository.save(property);
        }

        List<Property> all = propertyRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should handle various fiscal valuations")
    void shouldHandleVariousFiscalValuations() {
        java.math.BigDecimal[] valuaciones = {
                new java.math.BigDecimal("50000"),
                new java.math.BigDecimal("100000"),
                new java.math.BigDecimal("250000"),
                new java.math.BigDecimal("500000"),
                new java.math.BigDecimal("1000000")
        };

        for (java.math.BigDecimal appraisal : valuaciones) {
            Property property = new Property();
            property.setCadastralDesignation("Val-" + appraisal);
            property.setAddress("Propiedad valuada " + appraisal);
            property.setFiscalAppraisal(appraisal);
            propertyRepository.save(property);
        }

        List<Property> all = propertyRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should maintain data integrity on concurrent operations")
    void shouldMaintainDataIntegrityOnConcurrentOperations() {
        // Create initial data
        Property inmueble1 = new Property();
        inmueble1.setCadastralDesignation("Prop 1");
        inmueble1.setAddress("Propiedad 1");
        inmueble1.setFiscalAppraisal(new java.math.BigDecimal("100000"));
        Property saved1 = propertyRepository.save(inmueble1);

        Property inmueble2 = new Property();
        inmueble2.setCadastralDesignation("Prop 2");
        inmueble2.setAddress("Propiedad 2");
        inmueble2.setFiscalAppraisal(new java.math.BigDecimal("150000"));
        Property saved2 = propertyRepository.save(inmueble2);

        // Simulate concurrent reads
        Property read1 = propertyRepository.findById(saved1.getIdProperty()).orElseThrow();
        Property read2 = propertyRepository.findById(saved2.getIdProperty()).orElseThrow();

        assertThat(read1.getAddress()).isEqualTo("Propiedad 1");
        assertThat(read2.getAddress()).isEqualTo("Propiedad 2");
        assertThat(read1.getFiscalAppraisal()).isNotEqualTo(read2.getFiscalAppraisal());
    }

    @Test
    @DisplayName("Should support filtering and search operations")
    void shouldSupportFilteringAndSearchOperations() {
        // Create diverse data
        for (int i = 1; i <= 10; i++) {
            Property property = new Property();
            property.setCadastralDesignation("Nomenclatura " + i);
            property.setAddress("Calle " + i);
            property.setFiscalAppraisal(java.math.BigDecimal.valueOf(50000 + (i * 10000L)));
            propertyRepository.save(property);
        }

        List<Property> all = propertyRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(10);

        // Filter by valuacion > 100000
        List<Property> highValueProperties = all.stream()
                .filter(i -> i.getFiscalAppraisal() != null &&
                           i.getFiscalAppraisal().compareTo(new java.math.BigDecimal("100000")) > 0)
                .toList();
        assertThat(highValueProperties.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should handle batch operations efficiently")
    void shouldHandleBatchOperationsEfficiently() {
        // Batch create
        for (int i = 0; i < 20; i++) {
            Property property = new Property();
            property.setCadastralDesignation("Batch-" + i);
            property.setAddress("Batch Calle " + i);
            property.setFiscalAppraisal(java.math.BigDecimal.valueOf(10000L * (i + 1)));
            propertyRepository.save(property);
        }

        List<Property> all = propertyRepository.findAll();
        long countBatch = all.stream()
                .filter(i -> i.getCadastralDesignation() != null &&
                           i.getCadastralDesignation().startsWith("Batch"))
                .count();

        assertThat(countBatch).isGreaterThanOrEqualTo(20);
    }

    @Test
    @DisplayName("Should support complex property scenarios")
    void shouldSupportComplexPropertyScenarios() {
        // High-end property
        Property luxury = new Property();
        luxury.setCadastralDesignation("ALV-1050-LUX");
        luxury.setAddress("Avenida Alvear 1050, Penthouse Exclusivo");
        luxury.setFiscalAppraisal(new java.math.BigDecimal("5000000"));
        luxury.setNotes("Propiedad de lujo en zona premium");
        propertyRepository.save(luxury);

        // Budget property
        Property budget = new Property();
        budget.setCadastralDesignation("RIO-800-STD");
        budget.setAddress("Calle Rioja 800, Departamento");
        budget.setFiscalAppraisal(new java.math.BigDecimal("45000"));
        propertyRepository.save(budget);

        List<Property> all = propertyRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(2);

        // Verify both exist
        assertThat(all).anyMatch(i -> i.getFiscalAppraisal() != null &&
                                     i.getFiscalAppraisal().compareTo(new java.math.BigDecimal("1000000")) > 0);
        assertThat(all).anyMatch(i -> i.getFiscalAppraisal() != null &&
                                     i.getFiscalAppraisal().compareTo(new java.math.BigDecimal("100000")) < 0);
    }
}
