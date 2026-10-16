package com.licensis.notaire.adapter.out.persistence.procedure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.procedure.ProcedureFolderOpsPort;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.dto.DtoProcedureFolder;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;

/**
 * Persistence/ops adapter for procedure folders. Holds entity access previously on
 * {@code ProcedureFolderController} (issue #1795).
 */
@Component
public class ProcedureFolderOpsAdapter implements ProcedureFolderOpsPort {

    private final ProcedureFolderService procedureFolderService;

    public ProcedureFolderOpsAdapter(ProcedureFolderService procedureFolderService) {
        this.procedureFolderService = procedureFolderService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoProcedureFolder> findById(Integer id) {
        return procedureFolderService.findById(id).map(ProcedureFolder::getDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoProcedureFolder> search(Integer managementId, Integer procedureId) {
        List<ProcedureFolder> folders;
        if (procedureId != null) {
            folders = procedureFolderService.findByProcedure(procedureId).map(List::of).orElse(List.of());
        } else if (managementId != null) {
            folders = procedureFolderService.findByManagement(managementId);
        } else {
            folders = List.of();
        }
        return folders.stream().map(ProcedureFolder::getDto).toList();
    }

    @Override
    public HoldResult putOnHold(Integer id, String reason) {
        try {
            return new HoldResult.Ok(procedureFolderService.putOnHold(id, reason).getDto());
        } catch (ResourceNotFoundException e) {
            return new HoldResult.NotFound();
        } catch (BusinessValidationException e) {
            return new HoldResult.BadRequest(e.getMessage());
        }
    }
}
