package com.licensis.notaire.unit.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.usecase.property.PropertyManagementService;
import com.licensis.notaire.application.usecase.property.PropertyQueryService;
import com.licensis.notaire.domain.property.Property;
import com.licensis.notaire.domain.property.PropertyDetails;
import com.licensis.notaire.domain.property.PropertyNotFoundException;
import com.licensis.notaire.testing.InMemoryPropertyCatalog;

/** The behaviour of the property catalog runs on plain objects: no Spring, no database, no mocks (issue #1702). */
@DisplayName("Property use cases (issue #1702)")
class PropertyUseCasesTest {

    private PropertyManagementService manage;
    private PropertyQueryService query;

    @BeforeEach
    void setUp() {
        InMemoryPropertyCatalog catalog = new InMemoryPropertyCatalog();
        manage = new PropertyManagementService(catalog);
        query = new PropertyQueryService(catalog);
    }

    private PropertyDetails details(String designation) {
        return new PropertyDetails(designation, new BigDecimal("10"), "St", null, null, null, null);
    }

    @Test
    @DisplayName("create stores the property and returns it with an id")
    void creates() {
        Property created = manage.create(details("CAD-1"));

        assertThat(created.id()).isNotNull();
        assertThat(query.getById(created.id()).details().cadastralDesignation()).isEqualTo("CAD-1");
        assertThat(query.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("create without a designation stores nothing")
    void createRejectsMissingDesignation() {
        assertThatThrownBy(() -> manage.create(details(" "))).isInstanceOf(IllegalArgumentException.class);
        assertThat(query.findAll()).isEmpty();
    }

    @Test
    @DisplayName("update replaces the details of an existing property")
    void updates() {
        Property created = manage.create(details("OLD"));

        manage.update(created.id(), details("NEW"));

        assertThat(query.getById(created.id()).details().cadastralDesignation()).isEqualTo("NEW");
    }

    @Test
    @DisplayName("update without a designation keeps the stored one")
    void updateRejectsMissingDesignation() {
        Property created = manage.create(details("KEEP"));

        assertThatThrownBy(() -> manage.update(created.id(), details(null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(query.getById(created.id()).details().cadastralDesignation()).isEqualTo("KEEP");
    }

    @Test
    @DisplayName("an unknown id is not found on read, update and delete")
    void unknownIsNotFound() {
        assertThatThrownBy(() -> query.getById(99)).isInstanceOf(PropertyNotFoundException.class);
        assertThatThrownBy(() -> manage.update(99, details("X"))).isInstanceOf(PropertyNotFoundException.class);
        assertThatThrownBy(() -> manage.delete(99)).isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    @DisplayName("delete removes the property")
    void deletes() {
        Property created = manage.create(details("DEL"));

        manage.delete(created.id());

        assertThat(query.findAll()).isEmpty();
    }
}
