package com.licensis.notaire.application.port.in.budget;

import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetCommand;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.SaveResult;

/**
 * Write-side inbound port for budgets (issue #1805).
 */
public interface ManageBudgetsUseCase {

    SaveResult create(BudgetCommand command);

    SaveResult update(Integer id, BudgetCommand command);

    DeleteResult delete(Integer id);
}
