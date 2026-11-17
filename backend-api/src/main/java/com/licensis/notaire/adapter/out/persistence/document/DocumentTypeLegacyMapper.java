package com.licensis.notaire.adapter.out.persistence.document;

import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.dto.DtoDocumentType;

/**
 * Maps the legacy {@link DocumentType} entity without leaving transport types on the entity (#580).
 */
public final class DocumentTypeLegacyMapper {

    private DocumentTypeLegacyMapper() {
    }

    public static DtoDocumentType toDto(DocumentType type) {
        DtoDocumentType dto = new DtoDocumentType();
        dto.setIdDocumentType(type.getIdDocumentType());
        dto.setName(type.getName());
        dto.setExpires(type.getExpires());
        if (dto.isExpires()) {
            dto.setDueDays(type.getDueDays());
        }
        dto.setDeliveredBy(type.getDeliveredBy());
        dto.setVersion(type.getVersion());
        dto.setEnabled(type.getEnabled());
        dto.setReturned(type.getReturned());
        return dto;
    }

    public static void apply(DocumentType type, DtoDocumentType dto) {
        if (dto.getIdDocumentType() != null) {
            type.setIdDocumentType(dto.getIdDocumentType());
        }
        type.setName(dto.getName());
        type.setExpires(dto.isExpires());
        type.setDueDays(type.getExpires() ? dto.getDueDays() : null);
        type.setDeliveredBy(dto.getDeliveredBy());
        if (dto.getVersion() != null) {
            type.setVersion(dto.getVersion());
        }
        if (dto.getEnabled() != null) {
            type.setEnabled(dto.getEnabled());
        }
        if (dto.getReturned() != null) {
            type.setReturned(dto.getReturned());
        }
    }
}
