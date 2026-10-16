package com.licensis.notaire.application.usecase.procedure;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.procedure.QueryProceduresUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.PageResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureView;

/**
 * Read-side use case for procedures — delegates to {@link ProcedureOpsPort} (issue #1801).
 */
@Service
public class ProcedureQueryUseCaseService implements QueryProceduresUseCase {

    private final ProcedureOpsPort ops;

    public ProcedureQueryUseCaseService(ProcedureOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public PageResult findAll(int page, int size, String sort) {
        return ops.findAll(page, size, sort);
    }

    @Override
    public Optional<ProcedureView> findById(Integer id) {
        return ops.findById(id);
    }
}
