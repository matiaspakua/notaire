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

@DisplayName("Property Repository Integration Tests")
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
        property.setCadastralDesignation("Designation 123");
        property.setAddress("Main Street 123, Apartment 4B");
        property.setFiscalAppraisal(new java.math.BigDecimal("100000"));
        property.setNotes("Test property");
        Property saved = propertyRepository.save(property);

        assertThat(saved.getIdProperty()).isNotNull();
        assertThat(saved.getAddress()).isEqualTo("Main Street 123, Apartment 4B");
        assertThat(saved.getCadastralDesignation()).isEqualTo("Designation 123");
        assertThat(saved.getFiscalAppraisal()).isEqualByComparingTo(new java.math.BigDecimal("100000"));
        assertThat(saved.getNotes()).isEqualTo("Test property");
    }

    @Test
    @DisplayName("Should retrieve property by ID")
    void shouldRetrievePropertyById() {
        Property property = new Property();
        property.setCadastralDesignation("Test 456");
        property.setAddress("Main Street 123, Apartment 4B");
        Property saved = propertyRepository.save(property);

        Optional<Property> found = propertyRepository.findById(saved.getIdProperty());

        assertThat(found).isPresent();
        assertThat(found.get().getAddress()).isEqualTo("Main Street 123, Apartment 4B");
    }

    @Test
    @DisplayName("Should find all properties")
    void shouldFindAllProperties() {
        Property property = new Property();
        property.setCadastralDesignation("Test 789");
        property.setAddress("Main Street 123, Apartment 4B");
        propertyRepository.save(property);

        List<Property> all = propertyRepository.findAll();

        assertThat(all).isNotEmpty();
        assertThat(all).anyMatch(i -> i.getAddress().equals("Main Street 123, Apartment 4B"));
    }

    @Test
    @DisplayName("Should update property")
    void shouldUpdateProperty() {
        Property property = new Property();
        property.setCadastralDesignation("Update Test");
        property.setAddress("Original Street");
        property.setFiscalAppraisal(new java.math.BigDecimal("100000"));
        Property saved = propertyRepository.save(property);

        saved.setAddress("Corrientes Avenue 1234");
        saved.setFiscalAppraisal(new java.math.BigDecimal("150000"));
        Property updated = propertyRepository.save(saved);

        assertThat(updated.getAddress()).isEqualTo("Corrientes Avenue 1234");
        assertThat(updated.getFiscalAppraisal()).isEqualByComparingTo(new java.math.BigDecimal("150000"));

        Optional<Property> fetched = propertyRepository.findById(saved.getIdProperty());
        assertThat(fetched.get().getAddress()).isEqualTo("Corrientes Avenue 1234");
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
        property.setAddress("Minimum address");
        property.setFiscalAppraisal(null);
        property.setNotes(null);

        Property saved = propertyRepository.save(property);

        assertThat(saved.getFiscalAppraisal()).isNull();
        assertThat(saved.getNotes()).isNull();
    }

    @Test
    @DisplayName("Should support multiple properties")
    void shouldSupportMultipleProperties() {
        for (int i = 0; i < 5; i++) {
            Property property = new Property();
            property.setCadastralDesignation("Designation " + i);
            property.setAddress("Street " + i);
            property.setFiscalAppraisal(java.math.BigDecimal.valueOf(50000 + (i * 10000L)));
            propertyRepository.save(property);
        }

        List<Property> all = propertyRepository.findAll();
        assertThat(all.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle long address strings")
    void shouldHandleLongAddressStrings() {
        Property property = new Property();
        String longAddress = "Street " + "X".repeat(100) + " Number 9999, Apartment 10Z, Floor 50";
        property.setCadastralDesignation("Long Address Test");
        property.setAddress(longAddress);

        Property saved = propertyRepository.save(property);

        assertThat(saved.getAddress()).startsWith("Street X");
        assertThat(saved.getAddress()).contains("Number 9999");
    }
}
