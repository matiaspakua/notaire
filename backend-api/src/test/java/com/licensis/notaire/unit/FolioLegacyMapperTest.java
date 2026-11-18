package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.folio.FolioLegacyMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.Person;

@DisplayName("Folio mapping lives outside the entity")
class FolioLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyFolioFieldsOntoTheDto")
    void shouldCopyFolioFieldsOntoTheDto() {
        Person notary = new Person();
        notary.setPersonId(11);
        notary.setNotaryRegistrationNumber(4);
        FolioType type = new FolioType(7);
        type.setName("Protocol");
        type.setNotes("type notes");
        type.setEnabled(true);
        type.setIsAuxiliary(false);
        type.setVersion(2);
        Deed deed = new Deed(9);
        deed.setNumber(3001);
        deed.setStatus("OPEN");

        Folio folio = new Folio();
        folio.setIdFolio(5);
        folio.setNumber(101);
        folio.setYear(2025);
        folio.setStatus("Used");
        folio.setNotes("notes");
        folio.setVersion(1);
        folio.setFkIdNotaryPerson(notary);
        folio.setFkIdFolioType(type);
        folio.setFkIdDeed(deed);

        var dto = FolioLegacyMapper.toDto(folio);

        assertThat(dto.getIdFolio()).isEqualTo(5);
        assertThat(dto.getNumber()).isEqualTo(101);
        assertThat(dto.getYear()).isEqualTo(2025);
        assertThat(dto.getStatus()).isEqualTo("Used");
        assertThat(dto.getNotes()).isEqualTo("notes");
        assertThat(dto.getVersion()).isEqualTo(1);
        assertThat(dto.getPersonNotary().getId()).isEqualTo(11);
        assertThat(dto.getPersonNotary().getNotaryRegistrationNumber()).isEqualTo(4);
        assertThat(dto.getFolioType().getIdFolioType()).isEqualTo(7);
        assertThat(dto.getFolioType().getName()).isEqualTo("Protocol");
        assertThat(dto.getFolioType().isIsAuxiliary()).isFalse();
        assertThat(dto.getDeed().getIdDeed()).isEqualTo(9);
        assertThat(dto.getDeed().getNumber()).isEqualTo(3001);
        assertThat(dto.getDeed().getStatus()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("shouldLeaveTheDeedEmptyWhenTheFolioHasNone")
    void shouldLeaveTheDeedEmptyWhenTheFolioHasNone() {
        Folio folio = new Folio();
        folio.setFkIdFolioType(new FolioType("Protocol"));

        assertThat(FolioLegacyMapper.toDto(folio).getDeed()).isNull();
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfFolio")
    void shouldKeepDtoTypesOutOfFolio() {
        assertThat(Arrays.stream(Folio.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto");
    }
}
