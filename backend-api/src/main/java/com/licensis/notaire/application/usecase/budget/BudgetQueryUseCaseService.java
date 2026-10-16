package com.licensis.notaire.application.usecase.budget;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.budget.QueryBudgetsUseCase;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetView;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.PageResult;

/**
 * Read-side use case for budgets — delegates to {@link BudgetOpsPort} (issue #1805).
 */
@Service
public class BudgetQueryUseCaseService implements QueryBudgetsUseCase {

    private final BudgetOpsPort ops;

    public BudgetQueryUseCaseService(BudgetOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public PageResult findAll(int page, int size, String sort) {
        return ops.findAll(page, size, sort);
    }

    @Override
    public Optional<BudgetView> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    public List<BudgetView> findByPerson(Integer personId) {
        return ops.findByPerson(personId);
    }

    @Override
    public List<BudgetView> findByStatus(String status) {
        return ops.findByStatus(status);
    }
}
