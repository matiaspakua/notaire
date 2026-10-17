package com.licensis.notaire.unit.persistence;

import com.licensis.notaire.persistence.PersistenceConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PersistenceConstants residual cleanup (#1918)")
class PersistenceConstantsTest {

    @Test
    @DisplayName("INITIAL_VERSION should be 0 outside jpa package")
    void initialVersionShouldBeZero() {
        assertThat(PersistenceConstants.INITIAL_VERSION).isZero();
    }

    @Test
    @DisplayName("legacy jpa.PersistenceConstants must be gone")
    void legacyJpaPersistenceConstantsMustBeGone() {
        assertThatThrownBy(() -> Class.forName("com.licensis.notaire.jpa.PersistenceConstants"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("dead SessionAdministrator must be gone")
    void sessionAdministratorMustBeGone() {
        assertThatThrownBy(() -> Class.forName("com.licensis.notaire.service.SessionAdministrator"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("dead ValidationAdministrator must be gone")
    void validationAdministratorMustBeGone() {
        assertThatThrownBy(() -> Class.forName("com.licensis.notaire.service.ValidationAdministrator"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
