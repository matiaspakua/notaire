package com.licensis.notaire.application.usecase.procedure;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.procedure.ManageProceduresUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureCommand;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult;

/**
 * Write-side use case for procedures — delegates to {@link ProcedureOpsPort} (issue #1801).
 */
@Service
public class ProcedureWriteService implements ManageProceduresUseCase {

    private final ProcedureOpsPort ops;

    public ProcedureWriteService(ProcedureOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(ProcedureCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, ProcedureCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
