package com.licensis.notaire.application.usecase.procedure;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.procedure.ManageProcedureTypesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.WorkflowAssignResult;
import com.licensis.notaire.dto.DtoProcedureType;

/**
 * Write-side use case for procedure types — delegates to {@link ProcedureTypeOpsPort} (issue #1791).
 */
@Service
public class ProcedureTypeWriteService implements ManageProcedureTypesUseCase {

    private final ProcedureTypeOpsPort ops;

    public ProcedureTypeWriteService(ProcedureTypeOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(DtoProcedureType dto) {
        return ops.create(dto);
    }

    @Override
    public SaveResult update(Integer id, DtoProcedureType dto) {
        return ops.update(id, dto);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }

    @Override
    public WorkflowAssignResult assignWorkflow(Integer id, Integer workflowDefinitionId) {
        return ops.assignWorkflow(id, workflowDefinitionId);
    }
}
