package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.ProcedureType;

public record ProcedureTypeRef(Integer idProcedureType, String name, String notes, boolean isArchived,
        boolean isRegistered, boolean associatesProperties, boolean enabled) {

    public static ProcedureTypeRef from(ProcedureType type) {
        return type == null ? null : new ProcedureTypeRef(type.getIdProcedureType(), type.getName(),
                type.getNotes(), type.getIsArchived(), type.getIsRegistered(), type.getAssociatesProperties(),
                type.getEnabled());
    }
}
