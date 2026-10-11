package com.licensis.notaire.service.unit;

import com.licensis.notaire.business.FolioType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FolioType Unit Tests")
class FolioTypeTest {

    @Test
    @DisplayName("A folio type not marked as auxiliary defaults to Protocolo Principal")
    void shouldDefaultIsAuxiliaryToFalse() {
        FolioType folioType = new FolioType("Protocolo Principal");

        assertThat(folioType.isIsAuxiliary()).isFalse();
    }

    @Test
    @DisplayName("A folio type can be marked as belonging to Protocolo Auxiliar")
    void shouldMarkFolioTypeAsAuxiliary() {
        FolioType folioType = new FolioType("Protocolo Auxiliar");

        folioType.setIsAuxiliary(true);

        assertThat(folioType.isIsAuxiliary()).isTrue();
    }
}
