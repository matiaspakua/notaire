package com.licensis.notaire.integration;

import com.licensis.notaire.business.Property;
import com.licensis.notaire.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inmueble Repository Integration Tests")
class PropertyRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist property with all fields")
    void shouldPersistPropertyWithAllFields() {
        Property property = new Property();
        property.setCadastralDesignation("Nomenclatura 123");
        property.setAddress("Calle Principal 123, Apartamento 4B");
        property.setFiscalAppraisal(100000f);
        property.setNotes("Test property");
        Property saved = propertyRepository.save(property);

        assertThat(saved.getIdProperty()).isNotNull();
        assertThat(saved.getAddress()).isEqualTo("Calle Principal 123, Apartamento 4B");
        assertThat(saved.getCadastralDesignation()).isEqualTo("Nomenclatura 123");
        assertThat(saved.getFiscalAppraisal()).isEqualTo(100000f);
        assertThat(saved.getNotes()).isEqualTo("Test property");
    }

    @Test
    @DisplayName("Should retrieve property by ID")
    void shouldRetrievePropertyById() {
        Property property = new Property();
        property.setCadastralDesignation("Test 456");
        property.setAddress("Calle Principal 123, Apartamento 4B");
        Property saved = propertyRepository.save(property);

        Optional<Property> found = propertyRepository.findById(saved.getIdProperty());

        assertThat(found).isPresent();
        assertThat(found.get().getAddress()).isEqualTo("Calle Principal 123, Apartamento 4B");
    }

    @Test
    @DisplayName("Should find all inmuebles")
    void shouldFindAllProperties() {
        Property property = new Property();
        property.setCadastralDesignation("Test 789");
        property.setAddress("Calle Principal 123, Apartamento 4B");
        propertyRepository.save(property);

        List<Property> all = propertyRepository.findAll();

        assertThat(all).isNotEmpty();
        assertThat(all).anyMatch(i -> i.getAddress().equals("Calle Principal 123, Apartamento 4B"));
    }

    @Test
    @DisplayName("Should update property")
    void shouldUpdateProperty() {
        Property property = new Property();
        property.setCadastralDesignation("Update Test");
        property.setAddress("Original Street");
        property.setFiscalAppraisal(100000f);
        Property saved = propertyRepository.save(property);

        saved.setAddress("Avenida Corrientes 1234");
        saved.setFiscalAppraisal(150000f);
        Property updated = propertyRepository.save(saved);

        assertThat(updated.getAddress()).isEqualTo("Avenida Corrientes 1234");
        assertThat(updated.getFiscalAppraisal()).isEqualTo(150000f);

        Optional<Property> fetched = propertyRepository.findById(saved.getIdProperty());
        assertThat(fetched.get().getAddress()).isEqualTo("Avenida Corrientes 1234");
    }

    @Test
    @DisplayName("Should delete property")
    void shouldDeleteProperty() {
        Property property = new Property();
        property.setCadastralDesignation("Delete Test");
        property.setAddress("To Be Deleted");
        Property saved = propertyRepository.save(property);
        Integer id = saved.getIdProperty();

        propertyRepository.delete(saved);

        Optional<Property> deleted = propertyRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should handle nullable fields")
    void shouldHandleNullableFields() {
        Property property = new Property();
        property.setCadastralDesignation("Test 456");
        property.setAddress("Dirección Mínima");
        property.setFiscalAppraisal(null);
        property.setNotes(null);

        Property saved = propertyRepository.save(property);

        assertThat(saved.getFiscalAppraisal()).isNull();
        assertThat(saved.getNotes()).isNull();
    }

    @Test
    @DisplayName("Should support multiple inmuebles")
    void shouldSupportMultipleProperties() {
        for (int i = 0; i < 5; i++) {
            Property property = new Property();
            property.setCadastralDesignation("Nomenclatura " + i);
            property.setAddress("Calle " + i);
            property.setFiscalAppraisal((float) (50000 + (i * 10000)));
            propertyRepository.save(property);
        }

        List<Property> all = propertyRepository.findAll();
        assertThat(all.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle long address strings")
    void shouldHandleLongAddressStrings() {
        Property property = new Property();
        String longAddress = "Calle " + "X".repeat(100) + " Número 9999, Apartamento 10Z, Piso 50";
        property.setCadastralDesignation("Long Address Test");
        property.setAddress(longAddress);

        Property saved = propertyRepository.save(property);

        assertThat(saved.getAddress()).startsWith("Calle X");
        assertThat(saved.getAddress()).contains("Número 9999");
    }
}
