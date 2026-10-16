package com.licensis.notaire.application.port.in.budget;

import java.util.List;

import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;

/**
 * Inbound port for CU39 — load budget-template concepts as items on a budget (issue #1865).
 */
public interface LoadBudgetItemsFromTemplateUseCase {

    List<ItemView> loadItemsFromTemplate(Integer idBudget, Integer typeProcedureId);
}
