package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.DocumentType;

public record DocumentTypeRef(Integer idDocumentType, String name, boolean expires, Integer dueDays,
        String deliveredBy, boolean returned, boolean enabled) {

    public static DocumentTypeRef from(DocumentType type) {
        return type == null ? null : new DocumentTypeRef(type.getIdDocumentType(), type.getName(),
                type.getExpires(), type.getDueDays(), type.getDeliveredBy(), type.getReturned(),
                type.getEnabled());
    }
}
