package com.licensis.notaire.adapter.in.web.item;

import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.dto.TypeItem;

/**
 * Wire shape for budget items. Maps from {@link ItemView} only (issue #1826 / ArchUnit R1).
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
