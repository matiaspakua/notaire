package com.licensis.notaire.application.port.in.budget;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetView;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.PageResult;

/**
 * Read-side inbound port for budgets (issue #1805).
 */
public interface QueryBudgetsUseCase {

    PageResult findAll(int page, int size, String sort);

    Optional<BudgetView> findById(Integer id);

    List<BudgetView> findByPerson(Integer personId);

    List<BudgetView> findByStatus(String status);
}
