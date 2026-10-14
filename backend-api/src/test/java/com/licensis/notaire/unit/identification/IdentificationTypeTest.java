package com.licensis.notaire.unit.identification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.domain.identification.IdentificationType;

@DisplayName("IdentificationType domain model (issue #1700)")
class IdentificationTypeTest {

    @Test
    @DisplayName("create trims the name and starts without an id")
    void createTrims() {
        IdentificationType type = IdentificationType.create("  DNI  ", "8");

        assertThat(type.name()).isEqualTo("DNI");
        assertThat(type.characters()).isEqualTo("8");
        assertThat(type.id()).isNull();
    }

    @Test
    @DisplayName("create rejects a blank name")
    void createRejectsBlank() {
        assertThatThrownBy(() -> IdentificationType.create("   ", "8")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdentificationType.create(null, "8")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("restore accepts legacy rows without validating them")
    void restoreDoesNotValidate() {
        IdentificationType legacy = IdentificationType.restore(7, " ", null, 3);

        assertThat(legacy.id()).isEqualTo(7);
        assertThat(legacy.version()).isEqualTo(3);
    }

    @Test
    @DisplayName("hasSameNameAs ignores letter case and surrounding spaces")
    void sameNameIgnoresCaseAndSpaces() {
        IdentificationType type = IdentificationType.create("DNI", "8");

        assertThat(type.hasSameNameAs(" dni ")).isTrue();
        assertThat(type.hasSameNameAs("CUIT")).isFalse();
    }

    @Test
    @DisplayName("changeTo keeps the id and version and replaces name and characters")
    void changeToKeepsIdentity() {
        IdentificationType type = IdentificationType.restore(4, "DNI", "8", 2).changeTo(" Passport ", "9");

        assertThat(type.id()).isEqualTo(4);
        assertThat(type.version()).isEqualTo(2);
        assertThat(type.name()).isEqualTo("Passport");
        assertThat(type.characters()).isEqualTo("9");
    }
}
