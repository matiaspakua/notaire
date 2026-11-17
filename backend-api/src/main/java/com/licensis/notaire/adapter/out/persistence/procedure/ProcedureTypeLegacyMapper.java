package com.licensis.notaire.adapter.out.persistence.procedure;

import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.dto.DtoProcedureType;

/**
 * Maps the legacy {@link ProcedureType} entity without leaving transport types on the entity (#580).
 */
public final class ProcedureTypeLegacyMapper {

    private ProcedureTypeLegacyMapper() {
    }

    public static DtoProcedureType toDto(ProcedureType type) {
        DtoProcedureType dto = new DtoProcedureType();
        dto.setIdProcedureType(type.getIdProcedureType());
        dto.setName(type.getName());
        dto.setIsArchived(type.getIsArchived());
        dto.setIsRegistered(type.getIsRegistered());
        dto.setAssociatesProperties(type.getAssociatesProperties());
        dto.setNotes(type.getNotes());
        dto.setVersion(type.getVersion());
        dto.setEnabled(type.getEnabled());
        if (type.getWorkflowDefinition() != null) {
            dto.setWorkflowDefinitionId(type.getWorkflowDefinition().getId());
            dto.setWorkflowDefinitionName(type.getWorkflowDefinition().getName());
        }
        return dto;
    }

    public static void apply(ProcedureType type, DtoProcedureType dto) {
        if (!Boolean.TRUE.equals(dto.isValid())) {
            return;
        }
        type.setIdProcedureType(dto.getIdProcedureType());
        type.setName(dto.getName());
        type.setIsArchived(Boolean.TRUE.equals(dto.getIsArchived()));
        type.setIsRegistered(Boolean.TRUE.equals(dto.getIsRegistered()));
        type.setAssociatesProperties(Boolean.TRUE.equals(dto.getAssociatesProperties()));
        type.setNotes(dto.getNotes());
        if (dto.getVersion() != null) {
            type.setVersion(dto.getVersion());
        }
        type.setEnabled(!Boolean.FALSE.equals(dto.getEnabled()));
    }
}
