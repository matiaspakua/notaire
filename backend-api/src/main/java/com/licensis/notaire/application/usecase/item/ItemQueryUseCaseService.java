package com.licensis.notaire.application.usecase.item;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.item.QueryItemsUseCase;
import com.licensis.notaire.application.port.out.item.ItemOpsPort;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ListResult;

/**
 * Read-side use case for items — delegates to {@link ItemOpsPort} (issue #1809).
 */
@Service
public class ItemQueryUseCaseService implements QueryItemsUseCase {

    private final ItemOpsPort ops;

    public ItemQueryUseCaseService(ItemOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemView> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemView> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemView> findByBudget(Integer budgetId) {
        return ops.findByBudget(budgetId);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResult findDiscountsAndSurchargesByBudget(Integer budgetId) {
        return ops.findDiscountsAndSurchargesByBudget(budgetId);
    }
}
