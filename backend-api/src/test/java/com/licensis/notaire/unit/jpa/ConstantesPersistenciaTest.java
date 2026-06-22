package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.ConstantesPersistencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConstantesPersistencia constants unit tests")
class ConstantesPersistenciaTest {

    @Test
    @DisplayName("VERSION_INICIAL should be 0")
    void versionInicialShouldBeZero() {
        assertThat(ConstantesPersistencia.VERSION_INICIAL).isZero();
    }
}
