package com.licensis.notaire.application.usecase.budget;

import java.util.List;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.budget.QueryBudgetTemplatesUseCase;
import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort;
import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort.TemplateView;

/**
 * Read-side use case for budget templates — delegates to {@link BudgetTemplateOpsPort} (issue #1803).
 */
@Service
public class BudgetTemplateQueryUseCaseService implements QueryBudgetTemplatesUseCase {

    private final BudgetTemplateOpsPort ops;

    public BudgetTemplateQueryUseCaseService(BudgetTemplateOpsPort ops) {
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
}
