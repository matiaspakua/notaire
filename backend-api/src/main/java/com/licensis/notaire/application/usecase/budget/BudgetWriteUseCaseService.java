package com.licensis.notaire.application.usecase.budget;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.budget.ManageBudgetsUseCase;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetCommand;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.SaveResult;

/**
 * Write-side use case for budgets — delegates to {@link BudgetOpsPort} (issue #1805).
 */
@Service
public class BudgetWriteUseCaseService implements ManageBudgetsUseCase {

    private final BudgetOpsPort ops;

    public BudgetWriteUseCaseService(BudgetOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(BudgetCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, BudgetCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
