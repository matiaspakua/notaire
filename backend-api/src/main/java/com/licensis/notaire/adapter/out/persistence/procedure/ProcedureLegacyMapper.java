package com.licensis.notaire.adapter.out.persistence.procedure;

import java.util.ArrayList;

import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.dto.DtoBudget;
import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoProcedure;
import com.licensis.notaire.dto.DtoProcedureType;
import com.licensis.notaire.dto.DtoProperty;

/** Transport mapping for {@link Procedure}. The entity no longer builds {@link DtoProcedure}. */
public final class ProcedureLegacyMapper {

    private ProcedureLegacyMapper() {
    }

    public static DtoProcedure toDto(Procedure procedure) {
        DtoProcedure dto = new DtoProcedure();
        dto.setIdProcedure(procedure.getIdProcedure());
        dto.setNotes(procedure.getNotes());
        var type = procedure.getFkIdProcedureType();
        DtoProcedureType procedureTypeDto = new DtoProcedureType();
        procedureTypeDto.setIdProcedureType(type.getIdProcedureType());
        procedureTypeDto.setName(type.getName());
        procedureTypeDto.setIsArchived(type.getIsArchived());
        procedureTypeDto.setIsRegistered(type.getIsRegistered());
        procedureTypeDto.setAssociatesProperties(type.getAssociatesProperties());
        procedureTypeDto.setNotes(type.getNotes());
        procedureTypeDto.setVersion(type.getVersion());
        procedureTypeDto.setEnabled(type.getEnabled());
        if (type.getWorkflowDefinition() != null) {
            procedureTypeDto.setWorkflowDefinitionId(type.getWorkflowDefinition().getId());
            procedureTypeDto.setWorkflowDefinitionName(type.getWorkflowDefinition().getName());
        }
        dto.setProcedureType(procedureTypeDto);

        if (procedure.getFkIdDeed() != null) {
            DtoDeed deedDto = new DtoDeed();
            deedDto.setIdDeed(procedure.getFkIdDeed().getIdDeed());
            deedDto.setNumber(procedure.getFkIdDeed().getNumber());
            dto.setDeed(deedDto);
        }

        if (procedure.getFkIdManagement() != null) {
            var management = procedure.getFkIdManagement();
            DtoDeedManagement deedManagementDto = new DtoDeedManagement();
            deedManagementDto.setIdManagement(management.getIdManagement());
            deedManagementDto.setNumber(management.getNumber());
            DtoPerson notary = new DtoPerson();
            notary.setId(management.getFkIdNotaryPerson().getPersonId());
            notary.setNotaryRegistrationNumber(management.getFkIdNotaryPerson().getNotaryRegistrationNumber());
            deedManagementDto.setPersonNotary(notary);
            dto.setDeedManagement(deedManagementDto);
        }

        if (procedure.getFkIdProperty() != null) {
            var property = procedure.getFkIdProperty();
            DtoProperty propertyDto = new DtoProperty();
            propertyDto.setIdProperty(property.getIdProperty());
            propertyDto.setAddress(property.getAddress());
            propertyDto.setCadastralDesignation(property.getCadastralDesignation());
            propertyDto.setNotes(property.getNotes());
            propertyDto.setFiscalAppraisal(property.getFiscalAppraisal());
            propertyDto.setRegistrationNumber(property.getRegistrationNumber());
            propertyDto.setVolumeFolioLandRecord(property.getVolumeFolioLandRecord());
            propertyDto.setBoundaries(property.getBoundaries());
            dto.setProperty(propertyDto);
        } else {
            dto.setProperty(null);
        }

        if (procedure.getFkIdBudget() != null) {
            DtoBudget budget = new DtoBudget();
            budget.setIdBudget(procedure.getFkIdBudget().getIdBudget());
            dto.setBudget(budget);
        }

        procedure.setSubmittedDocumentList(new ArrayList<>());
        return dto;
    }
}
