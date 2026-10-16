package com.licensis.notaire.application.port.in.budget;

import java.util.List;

import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;

/**
 * Inbound port for CU71 — add catalog item copies to a budget (issue #1865).
 */
public interface AddBudgetItemsFromCatalogUseCase {

    List<ItemView> addItemsFromCatalog(Integer idBudget, List<Integer> idItems);
}
