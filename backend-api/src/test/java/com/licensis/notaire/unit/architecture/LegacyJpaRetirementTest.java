package com.licensis.notaire.unit.architecture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Gate 2 evidence for #576: legacy {@code jpa} / {@code JpaAdministrator} must leave the classpath.
 */
@RequirementCoverage({"CU76"})
@DisplayName("Legacy jpa retirement (#576)")
class LegacyJpaRetirementTest {

    @ParameterizedTest(name = "{0} must be absent")
    @ValueSource(strings = {
        "com.licensis.notaire.jpa.PersonJpaController",
        "com.licensis.notaire.jpa.IdentificationTypeJpaController",
        "com.licensis.notaire.service.JpaAdministrator",
        "com.licensis.notaire.service.LegacyJpaEmfConfig"
    })
    @DisplayName("legacy jpa/service bootstrap types are gone")
    void legacyBootstrapTypesMustBeAbsent(String className) {
        assertThatThrownBy(() -> Class.forName(className))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("jpa package root marker is gone")
    void jpaPersistenceMarkerMustBeAbsent() {
        assertThatThrownBy(() -> Class.forName("com.licensis.notaire.jpa.JpaPersistence"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
