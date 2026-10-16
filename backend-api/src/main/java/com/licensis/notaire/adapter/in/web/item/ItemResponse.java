package com.licensis.notaire.adapter.in.web.item;

import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.dto.TypeItem;

/**
 * Wire shape for budget items. {@link #from(Item)} remains for BudgetController template/catalog
 * endpoints that still return entities; ItemController maps from {@link ItemView} (issue #1809).
 */
public record ItemResponse(
        Integer idItem,
        String name,
        java.math.BigDecimal value,
        Integer percentage,
        String notes,
        TypeItem type,
        String reason,
        boolean fixedConcept,
        Integer budgetId,
        int version) {

    public static ItemResponse from(Item item) {
        Integer budgetId = item.getFkIdBudget() != null ? item.getFkIdBudget().getIdBudget() : null;
        return new ItemResponse(
                item.getIdItem(),
                item.getName(),
                item.getValue(),
                item.getPercentage(),
                item.getNotes(),
                item.getType(),
                item.getReason(),
                item.isFixed(),
                budgetId,
                item.getVersion());
    }

    public static ItemResponse from(ItemView view) {
        return new ItemResponse(
                view.idItem(),
                view.name(),
                view.value(),
                view.percentage(),
                view.notes(),
                view.type(),
                view.reason(),
                view.fixedConcept(),
                view.budgetId(),
                view.version());
    }
}
