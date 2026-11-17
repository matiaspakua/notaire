package com.licensis.notaire.adapter.out.persistence.folio;

import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.dto.DtoFolioType;
import com.licensis.notaire.dto.exceptions.InvalidDtoException;

/**
 * Maps the legacy {@link FolioType} entity without leaving transport types on the entity (#580).
 */
public final class FolioTypeLegacyMapper {

    private FolioTypeLegacyMapper() {
    }

    public static DtoFolioType toDto(FolioType type) {
        DtoFolioType dto = new DtoFolioType();
        dto.setIdFolioType(type.getIdFolioType());
        dto.setName(type.getName());
        dto.setNotes(type.getNotes());
        dto.setEnabled(type.getEnabled());
        dto.setIsAuxiliary(type.isIsAuxiliary());
        dto.setVersion(type.getVersion());
        return dto;
    }

    public static void apply(FolioType type, DtoFolioType dto) throws InvalidDtoException {
        if (!Boolean.TRUE.equals(dto.isValid())) {
            throw new InvalidDtoException("The indicated DTO is not valid.");
        }
        type.setIdFolioType(dto.getIdFolioType());
        type.setName(dto.getName());
        type.setNotes(dto.getNotes());
        type.setEnabled(dto.isEnabled());
        type.setIsAuxiliary(dto.isIsAuxiliary());
        type.setVersion(dto.getVersion());
    }
}
