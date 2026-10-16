package com.licensis.notaire.application.usecase.procedure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.procedure.QueryProcedureTypesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort;
import com.licensis.notaire.dto.DtoProcedureType;

/**
 * Read-side use case for procedure types — delegates to {@link ProcedureTypeOpsPort} (issue #1791).
 */
@Service
public class ProcedureTypeQueryUseCaseService implements QueryProcedureTypesUseCase {

    private final ProcedureTypeOpsPort ops;

    public ProcedureTypeQueryUseCaseService(ProcedureTypeOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoProcedureType> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoProcedureType> searchByName(String name) {
        return ops.searchByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoProcedureType> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Boolean> isInUse(Integer id) {
        return ops.isInUse(id);
    }
}
