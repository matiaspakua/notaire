package com.licensis.notaire.adapter.out.persistence.management;

import java.util.ArrayList;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoProcedure;

/**
 * Maps the legacy {@link DeedManagement} entity without leaving {@code getDto} on the entity (#2329).
 */
public final class DeedManagementLegacyMapper {

    private DeedManagementLegacyMapper() {
    }

    public static DtoDeedManagement toDto(DeedManagement management) {
        DtoDeedManagement dtoManagement = new DtoDeedManagement();

        dtoManagement.setVersion(management.getVersion());
        dtoManagement.setIdManagement(management.getIdManagement());
        dtoManagement.setNumber(management.getNumber());
        dtoManagement.setHeading(management.getHeading());
        dtoManagement.setDateStart(management.getDateStart());
        dtoManagement.setNotes(management.getNotes());

        // Use getDtoNotary (not Person.getDto) to avoid a circular mapping loop
        dtoManagement.setPersonNotary(management.getDtoNotary());

        ArrayList<DtoProcedure> procedureDtos = new ArrayList<>();
        if (management.getProcedureList() != null && !management.getProcedureList().isEmpty()) {
            for (int i = 0; i < management.getProcedureList().size(); i++) {
                procedureDtos.add(management.getDtoProcedure(management.getProcedureList().get(i)));
            }
            dtoManagement.setAssociatedProcedures(procedureDtos);
        }

        /*
         * People associated with this management. Clients are collected without
         * discriminating by procedure, so duplicates across procedures are removed.
         */
        ArrayList<DtoPerson> personDtos = new ArrayList<>();
        ArrayList<Integer> personIds = new ArrayList<>();

        if (management.getProcedureList() != null && !management.getProcedureList().isEmpty()) {
            for (int j = 0; j < management.getProcedureList().size(); j++) {
                for (int i = 0; i < management.getProcedureList().get(j).getPersonList().size(); i++) {
                    DtoPerson personDto = management.getInvolvedPersonDto(
                            management.getProcedureList().get(j).getPersonList().get(i));
                    if (!personIds.contains(personDto.getId())) {
                        personIds.add(personDto.getId());
                        personDtos.add(personDto);
                    }
                }
            }
            dtoManagement.setInvolvedClients(personDtos);
        }

        if (management.getFkIdManagementStatus() != null) {
            DtoManagementStatus statusDto = new DtoManagementStatus();
            statusDto.setIdManagementStatus(management.getFkIdManagementStatus().getIdManagementStatus());
            statusDto.setName(management.getFkIdManagementStatus().getName());
            statusDto.setNotes(management.getFkIdManagementStatus().getNotes());
            statusDto.setVersion(management.getFkIdManagementStatus().getVersion());
            dtoManagement.setStatus(statusDto);
        } else {
            dtoManagement.setStatus(null);
        }

        return dtoManagement;
    }
}
