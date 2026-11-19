package com.licensis.notaire.adapter.out.persistence.procedure;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.dto.DtoProcedureFolder;

/**
 * Maps a legacy {@link ProcedureFolder} entity onto its transport DTO.
 * The entity no longer builds the DTO itself (#580).
 */
public final class ProcedureFolderLegacyMapper {

    private ProcedureFolderLegacyMapper() {
    }

    public static DtoProcedureFolder toDto(ProcedureFolder folder) {
        DeedManagement management = folder.getFkIdManagement();
        Procedure procedure = folder.getFkIdProcedure();
        return new DtoProcedureFolder(
                folder.getIdFolder(),
                folder.getNumber(),
                folder.getStatus(),
                folder.getWaitReason(),
                management != null ? management.getIdManagement() : null,
                procedure != null ? procedure.getIdProcedure() : null);
    }
}
