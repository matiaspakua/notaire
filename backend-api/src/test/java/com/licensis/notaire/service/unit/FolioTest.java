package com.licensis.notaire.service.unit;

import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Folio Unit Tests — CU87 deed linking")
class FolioTest {

    @Test
    @DisplayName("Should round-trip deed through DtoFolio")
    void shouldRoundTripDeedThroughDto() {
        DtoDeed dtoDeed = new DtoDeed();
        dtoDeed.setIdDeed(42);

        DtoFolio dtoFolio = new DtoFolio();
        dtoFolio.setIdFolio(1);
        dtoFolio.setNumber(10);
        dtoFolio.setYear(2026);
        dtoFolio.setStatus("Utilizado");
        dtoFolio.setDeed(dtoDeed);

        Folio folio = new Folio();
        folio.setAtributos(dtoFolio);
        folio.setFkIdFolioType(new FolioType("Protocolo Principal"));

        assertThat(folio.getFkIdDeed()).isNotNull();
        assertThat(folio.getFkIdDeed().getIdDeed()).isEqualTo(42);
        assertThat(folio.getDto().getDeed().getIdDeed()).isEqualTo(42);
    }

    @Test
    @DisplayName("Should return null deed in DTO when folio is not linked")
    void shouldReturnNullDeedWhenNotLinked() {
        Folio folio = new Folio();
        folio.setIdFolio(2);
        folio.setNumber(20);
        folio.setYear(2026);
        folio.setStatus("Nuevo");
        folio.setFkIdFolioType(new FolioType("Protocolo Principal"));

        assertThat(folio.getFkIdDeed()).isNull();
        assertThat(folio.getDto().getDeed()).isNull();
    }
}
