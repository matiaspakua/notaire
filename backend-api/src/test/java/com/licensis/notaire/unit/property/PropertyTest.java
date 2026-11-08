package com.licensis.notaire.unit.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.domain.property.Property;
import com.licensis.notaire.domain.property.PropertyDetails;

@DisplayName("Property domain model (issue #1702)")
class PropertyTest {

    private PropertyDetails details(String designation) {
        return new PropertyDetails(designation, new BigDecimal("100.00"), "Main St", "n", "R-1", "V-2", "north");
    }

    @Test
    @DisplayName("create starts without an id at version 0")
    void createStartsNew() {
        Property property = Property.create(details("CAD-1"));

        assertThat(property.id()).isNull();
        assertThat(property.version()).isZero();
        assertThat(property.details().cadastralDesignation()).isEqualTo("CAD-1");
    }

    @Test
    @DisplayName("the cadastral designation is required, blank counts as missing")
    void designationRequired() {
        assertThatThrownBy(() -> Property.create(details(null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Property.create(details("   "))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("restore accepts a stored row without validating it")
    void restoreDoesNotValidate() {
        Property legacy = Property.restore(7, details(" "), 3);

        assertThat(legacy.id()).isEqualTo(7);
        assertThat(legacy.version()).isEqualTo(3);
    }

    @Test
    @DisplayName("replaceDetails keeps identity and version and replaces every field")
    void replaceKeepsIdentity() {
        Property replaced = Property.restore(4, details("OLD"), 2)
                .replaceDetails(new PropertyDetails("NEW", null, "Other St", null, null, null, null));

        assertThat(replaced.id()).isEqualTo(4);
        assertThat(replaced.version()).isEqualTo(2);
        assertThat(replaced.details().cadastralDesignation()).isEqualTo("NEW");
        assertThat(replaced.details().fiscalAppraisal()).isNull();
        assertThat(replaced.details().notes()).isNull();
    }
}
