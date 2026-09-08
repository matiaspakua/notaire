package com.licensis.notaire.service.unit;

import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.TipoDeFolio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Folio Unit Tests — CU87 escritura linking")
class FolioTest {

    @Test
    @DisplayName("Should round-trip escritura through DtoFolio")
    void shouldRoundTripEscrituraThroughDto() {
        DtoEscritura dtoEscritura = new DtoEscritura();
        dtoEscritura.setIdEscritura(42);

        DtoFolio dtoFolio = new DtoFolio();
        dtoFolio.setIdFolio(1);
        dtoFolio.setNumero(10);
        dtoFolio.setAnio(2026);
        dtoFolio.setEstado("Utilizado");
        dtoFolio.setEscritura(dtoEscritura);

        Folio folio = new Folio();
        folio.setAtributos(dtoFolio);
        folio.setFkIdTipoFolio(new TipoDeFolio("Protocolo Principal"));

        assertThat(folio.getFkIdEscritura()).isNotNull();
        assertThat(folio.getFkIdEscritura().getIdEscritura()).isEqualTo(42);
        assertThat(folio.getDto().getEscritura().getIdEscritura()).isEqualTo(42);
    }

    @Test
    @DisplayName("Should return null escritura in DTO when folio is not linked")
    void shouldReturnNullEscrituraWhenNotLinked() {
        Folio folio = new Folio();
        folio.setIdFolio(2);
        folio.setNumero(20);
        folio.setAnio(2026);
        folio.setEstado("Nuevo");
        folio.setFkIdTipoFolio(new TipoDeFolio("Protocolo Principal"));

        assertThat(folio.getFkIdEscritura()).isNull();
        assertThat(folio.getDto().getEscritura()).isNull();
    }
}
