package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.exceptions.InvalidDtoException;

/**
 * Maps the legacy {@link ManagementStatus} entity without leaving transport types on the entity (#580).
 */
public final class ManagementStatusLegacyMapper {

    private ManagementStatusLegacyMapper() {
    }

    public static DtoManagementStatus toDto(ManagementStatus status) {
        DtoManagementStatus dto = new DtoManagementStatus();
        dto.setIdManagementStatus(status.getIdManagementStatus());
        dto.setName(status.getName());
        dto.setNotes(status.getNotes());
        dto.setVersion(status.getVersion());
        return dto;
    }

    public static void apply(ManagementStatus status, DtoManagementStatus dto) throws InvalidDtoException {
        if (!Boolean.TRUE.equals(dto.isValid())) {
            throw new InvalidDtoException("Invalid DTO");
        }
        status.setIdManagementStatus(dto.getIdManagementStatus());
        status.setName(dto.getName());
        status.setNotes(dto.getNotes());
        status.setVersion(dto.getVersion());
    }
}
