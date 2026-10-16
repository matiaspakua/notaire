package com.licensis.notaire.application.port.in.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureCommand;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult;

/**
 * Write-side inbound port for procedures (issue #1801).
 */
public interface ManageProceduresUseCase {

    SaveResult create(ProcedureCommand command);

    SaveResult update(Integer id, ProcedureCommand command);

    DeleteResult delete(Integer id);
}
