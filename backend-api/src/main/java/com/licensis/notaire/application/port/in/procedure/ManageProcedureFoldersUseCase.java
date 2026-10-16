package com.licensis.notaire.application.port.in.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureFolderOpsPort.HoldResult;

/**
 * Inbound write port for procedure folders (issue #1795).
 */
public interface ManageProcedureFoldersUseCase {

    HoldResult putOnHold(Integer id, String reason);
}
