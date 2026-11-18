package com.licensis.notaire.adapter.out.persistence.deed;

import com.licensis.notaire.adapter.out.persistence.folio.FolioLegacyMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.dto.DtoDeed;

/**
 * Maps a legacy {@link Deed} entity onto its transport DTO.
 * The entity no longer builds the DTO itself (#580).
 */
public final class DeedLegacyMapper {

    private DeedLegacyMapper() {
    }

    public static DtoDeed toDto(Deed deed) {
        DtoDeed deedDto = new DtoDeed();
        deedDto.setIdDeed(deed.getIdDeed());
        deedDto.setDateDeedrecording(deed.getDateDeedrecording());
        deedDto.setNumber(deed.getNumber());
        deedDto.setBody(deed.getBody());
        deedDto.setStatus(deed.getStatus());
        deedDto.setRegistrationEntryNumber(deed.getRegistrationEntryNumber());
        deedDto.setDateRegistration(deed.getDateRegistration());
        deedDto.setVersion(deed.getVersion());

        var folioList = deed.getFolioList();
        if (folioList != null && !folioList.isEmpty()) {
            for (Folio folio : folioList) {
                deedDto.getFolios().add(FolioLegacyMapper.toDto(folio));
            }
        } else {
            deedDto.setFolios(null);
        }

        var procedureList = deed.getProcedureList();
        if (procedureList != null && !procedureList.isEmpty()) {
            for (Procedure procedure : procedureList) {
                deedDto.getProcedures().add(procedure.getDto());
            }
        } else {
            deedDto.setProcedures(null);
        }
        return deedDto;
    }
}
