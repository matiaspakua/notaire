package com.licensis.notaire.application.usecase.procedure;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.procedure.ManageProcedureTemplatesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.SaveResult;

/**
 * Write use-case service for procedure templates (issue #1798).
 */
@Service
public class ProcedureTemplateWriteService implements ManageProcedureTemplatesUseCase {

    private final ProcedureTemplateOpsPort ops;

    public ProcedureTemplateWriteService(ProcedureTemplateOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(Integer procedureTypeId, Integer documentTypeId, String notes) {
        return ops.create(procedureTypeId, documentTypeId, notes);
    }

    @Override
    public SaveResult update(Integer procedureTypeId, Integer documentTypeId, String notes) {
        return ops.update(procedureTypeId, documentTypeId, notes);
    }

    @Override
    public DeleteResult delete(Integer procedureTypeId, Integer documentTypeId) {
        return ops.delete(procedureTypeId, documentTypeId);
    }
}
