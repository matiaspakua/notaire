package com.licensis.notaire.service.unit;

import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Folio Unit Tests — CU87 deed linking")
class FolioTest {

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
