package com.licensis.notaire.application.port.out.procedure;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoProcedureFolder;

/**
 * Outbound persistence/ops port for procedure folders (issue #1795).
 * Returns DTO shapes only so the application layer stays free of entities.
 */
public interface ProcedureFolderOpsPort {

    sealed interface HoldResult {
        record Ok(DtoProcedureFolder dto) implements HoldResult {}

        record NotFound() implements HoldResult {}

        record BadRequest(String message) implements HoldResult {}
    }

    Optional<DtoProcedureFolder> findById(Integer id);

    /**
     * Prefer {@code procedureId} when both are set (matches prior controller behaviour).
     */
    List<DtoProcedureFolder> search(Integer managementId, Integer procedureId);

    HoldResult putOnHold(Integer id, String reason);
}
