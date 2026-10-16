package com.licensis.notaire.application.usecase.procedure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.procedure.QueryProcedureTemplatesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.TemplateView;

/**
 * Query use-case service for procedure templates (issue #1798).
 */
@Service
public class ProcedureTemplateQueryUseCaseService implements QueryProcedureTemplatesUseCase {

    private final ProcedureTemplateOpsPort ops;

    public ProcedureTemplateQueryUseCaseService(ProcedureTemplateOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public List<TemplateView> findAll() {
        return ops.findAll();
    }

    @Override
    public List<TemplateView> findByProcedureType(Integer procedureTypeId) {
        return ops.findByProcedureType(procedureTypeId);
    }

    @Override
    public Optional<TemplateView> findById(Integer procedureTypeId, Integer documentTypeId) {
        return ops.findById(procedureTypeId, documentTypeId);
    }
}
