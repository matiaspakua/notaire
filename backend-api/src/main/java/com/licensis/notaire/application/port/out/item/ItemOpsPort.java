package com.licensis.notaire.application.port.out.item;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.TypeItem;

/**
 * Outbound persistence/ops port for budget items (issue #1809).
 * Returns view/command shapes only so the application layer stays free of entities and Spring Data.
 */
public interface ItemOpsPort {

    record ItemView(
            Integer idItem,
            String name,
            java.math.BigDecimal value,
            Integer percentage,
            String notes,
            TypeItem type,
            String reason,
            boolean fixedConcept,
            Integer budgetId,
            int version) {}

    record ItemCommand(
            String name,
            java.math.BigDecimal value,
            Integer percentage,
            String notes,
            TypeItem type,
            String reason,
            Boolean fixedConcept,
            Integer budgetId) {}

    sealed interface SaveResult {
        record Created(ItemView view) implements SaveResult {}

        record Updated() implements SaveResult {}

        record BadRequest() implements SaveResult {}

        record NotFound() implements SaveResult {}

        record ServerError() implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record Conflict() implements DeleteResult {}
    }

    sealed interface ListResult {
        record Ok(List<ItemView> items) implements ListResult {}

        record NotFound() implements ListResult {}
    }

    List<ItemView> findAll();

    Optional<ItemView> findById(Integer id);

    List<ItemView> findByBudget(Integer budgetId);

    ListResult findDiscountsAndSurchargesByBudget(Integer budgetId);

    SaveResult create(ItemCommand command);

    SaveResult update(Integer id, ItemCommand command);

    DeleteResult delete(Integer id);
}
