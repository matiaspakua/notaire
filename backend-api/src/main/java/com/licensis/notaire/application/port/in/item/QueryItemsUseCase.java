package com.licensis.notaire.application.port.in.item;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ListResult;

/**
 * Read-side inbound port for budget items (issue #1809).
 */
public interface QueryItemsUseCase {

    List<ItemView> findAll();

    Optional<ItemView> findById(Integer id);

    List<ItemView> findByBudget(Integer budgetId);

    ListResult findDiscountsAndSurchargesByBudget(Integer budgetId);
}
