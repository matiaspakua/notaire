package com.licensis.notaire.application.port.in.procedure;

import java.util.Optional;

import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.PageResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureView;

/**
 * Read-side inbound port for procedures (issue #1801).
 */
public interface QueryProceduresUseCase {

    PageResult findAll(int page, int size, String sort);

    Optional<ProcedureView> findById(Integer id);
}
