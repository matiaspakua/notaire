package com.licensis.notaire.application.usecase.procedure;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.procedure.ManageProcedureFoldersUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureFolderOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureFolderOpsPort.HoldResult;

/**
 * Write use-case service for procedure folders (issue #1795).
 */
@Service
public class ProcedureFolderWriteService implements ManageProcedureFoldersUseCase {

    private final ProcedureFolderOpsPort ops;

    public ProcedureFolderWriteService(ProcedureFolderOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public HoldResult putOnHold(Integer id, String reason) {
        return ops.putOnHold(id, reason);
    }
}
