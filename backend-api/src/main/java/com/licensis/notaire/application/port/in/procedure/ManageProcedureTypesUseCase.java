package com.licensis.notaire.application.port.in.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.WorkflowAssignResult;
import com.licensis.notaire.dto.DtoProcedureType;

/**
 * Write-side inbound port for procedure types (issue #1791).
 */
public interface ManageProcedureTypesUseCase {

    SaveResult create(DtoProcedureType dto);

    SaveResult update(Integer id, DtoProcedureType dto);

    DeleteResult delete(Integer id);

    WorkflowAssignResult assignWorkflow(Integer id, Integer workflowDefinitionId);
}
