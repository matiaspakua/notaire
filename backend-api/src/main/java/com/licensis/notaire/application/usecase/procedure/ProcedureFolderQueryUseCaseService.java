package com.licensis.notaire.application.usecase.procedure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.procedure.QueryProcedureFoldersUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureFolderOpsPort;
import com.licensis.notaire.dto.DtoProcedureFolder;

/**
 * Query use-case service for procedure folders (issue #1795).
 */
@Service
public class ProcedureFolderQueryUseCaseService implements QueryProcedureFoldersUseCase {

    private final ProcedureFolderOpsPort ops;

    public ProcedureFolderQueryUseCaseService(ProcedureFolderOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public Optional<DtoProcedureFolder> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    public List<DtoProcedureFolder> search(Integer managementId, Integer procedureId) {
        return ops.search(managementId, procedureId);
    }
}
