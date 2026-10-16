package com.licensis.notaire.application.usecase.budget;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.budget.ManageBudgetTemplatesUseCase;
import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort;
import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort.TemplateView;

/**
 * Write-side use case for budget templates — delegates to {@link BudgetTemplateOpsPort} (issue #1803).
 */
@Service
public class BudgetTemplateWriteService implements ManageBudgetTemplatesUseCase {

    private final BudgetTemplateOpsPort ops;

    public BudgetTemplateWriteService(BudgetTemplateOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public TemplateView create(Integer procedureTypeId, Integer conceptId, String notes) {
        return ops.create(procedureTypeId, conceptId, notes);
    }

    @Override
    public void updateNotes(Integer procedureTypeId, Integer conceptId, String notes) {
        ops.updateNotes(procedureTypeId, conceptId, notes);
    }

    @Override
    public void delete(Integer procedureTypeId, Integer conceptId) {
        ops.delete(procedureTypeId, conceptId);
    }
}
