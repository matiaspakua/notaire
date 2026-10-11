package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.PersistenceConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PersistenceConstants constants unit tests")
class PersistenceConstantsTest {

    @Test
    @DisplayName("INITIAL_VERSION should be 0")
    void initialVersionShouldBeZero() {
        assertThat(PersistenceConstants.INITIAL_VERSION).isZero();
    }
}
