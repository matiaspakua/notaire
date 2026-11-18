package com.licensis.notaire.adapter.out.persistence.folio;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoFolioType;
import com.licensis.notaire.dto.DtoPerson;

/**
 * Maps a legacy {@link Folio} entity onto its transport DTO.
 * The entity no longer builds the DTO itself (#580). The nested deed is copied
 * field by field so this does not call {@code Deed.getDto} and re-enter the folio list.
 */
public final class FolioLegacyMapper {

    private FolioLegacyMapper() {
    }

    public static DtoFolio toDto(Folio folio) {
        DtoFolio folioDto = new DtoFolio();
        folioDto.setIdFolio(folio.getIdFolio());
        folioDto.setNumber(folio.getNumber());
        folioDto.setStatus(folio.getStatus());
        folioDto.setNotes(folio.getNotes());
        folioDto.setYear(folio.getYear());
        folioDto.setVersion(folio.getVersion());

        Person notary = folio.getFkIdNotaryPerson();
        if (notary != null) {
            DtoPerson person = new DtoPerson();
            person.setId(notary.getPersonId());
            person.setNotaryRegistrationNumber(notary.getNotaryRegistrationNumber());
            folioDto.setPersonNotary(person);
        }

        FolioType type = folio.getFkIdFolioType();
        DtoFolioType folioTypeDto = new DtoFolioType();
        folioTypeDto.setIdFolioType(type.getIdFolioType());
        folioTypeDto.setName(type.getName());
        folioTypeDto.setNotes(type.getNotes());
        folioTypeDto.setEnabled(type.getEnabled());
        folioTypeDto.setIsAuxiliary(type.isIsAuxiliary());
        folioTypeDto.setVersion(type.getVersion());
        folioDto.setFolioType(folioTypeDto);

        Deed deed = folio.getFkIdDeed();
        if (deed != null) {
            DtoDeed deedDto = new DtoDeed();
            deedDto.setIdDeed(deed.getIdDeed());
            deedDto.setNumber(deed.getNumber());
            deedDto.setStatus(deed.getStatus());
            folioDto.setDeed(deedDto);
        }
        return folioDto;
    }
}
