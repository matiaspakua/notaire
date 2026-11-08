package com.licensis.notaire.application.port.in.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.SaveResult;

/**
 * Inbound write port for procedure templates (issue #1798).
 */
public interface ManageProcedureTemplatesUseCase {

    SaveResult create(Integer procedureTypeId, Integer documentTypeId, String notes);

    SaveResult update(Integer procedureTypeId, Integer documentTypeId, String notes);

    DeleteResult delete(Integer procedureTypeId, Integer documentTypeId);
}
