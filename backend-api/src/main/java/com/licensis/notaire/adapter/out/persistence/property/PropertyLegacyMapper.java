package com.licensis.notaire.adapter.out.persistence.property;

import com.licensis.notaire.business.Property;
import com.licensis.notaire.dto.DtoProperty;

/**
 * Maps the legacy {@link Property} entity without leaving transport types on the entity (#580).
 */
public final class PropertyLegacyMapper {

    private PropertyLegacyMapper() {
    }

    public static DtoProperty toDto(Property property) {
        DtoProperty dto = new DtoProperty();
        dto.setIdProperty(property.getIdProperty());
        dto.setAddress(property.getAddress());
        dto.setCadastralDesignation(property.getCadastralDesignation());
        dto.setNotes(property.getNotes());
        dto.setFiscalAppraisal(property.getFiscalAppraisal());
        dto.setRegistrationNumber(property.getRegistrationNumber());
        dto.setVolumeFolioLandRecord(property.getVolumeFolioLandRecord());
        dto.setBoundaries(property.getBoundaries());
        return dto;
    }
}
