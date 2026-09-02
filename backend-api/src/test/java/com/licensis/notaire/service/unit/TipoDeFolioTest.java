package com.licensis.notaire.service.unit;

import com.licensis.notaire.negocio.TipoDeFolio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoDeFolio Unit Tests")
class TipoDeFolioTest {

    @Test
    @DisplayName("A tipo de folio not marked as auxiliar defaults to Protocolo Principal")
    void shouldDefaultEsAuxiliarToFalse() {
        TipoDeFolio tipoDeFolio = new TipoDeFolio("Protocolo Principal");

        assertThat(tipoDeFolio.isEsAuxiliar()).isFalse();
    }

    @Test
    @DisplayName("A tipo de folio can be marked as belonging to Protocolo Auxiliar")
    void shouldMarkTipoDeFolioAsAuxiliar() {
        TipoDeFolio tipoDeFolio = new TipoDeFolio("Protocolo Auxiliar");

        tipoDeFolio.setEsAuxiliar(true);

        assertThat(tipoDeFolio.isEsAuxiliar()).isTrue();
    }
}
