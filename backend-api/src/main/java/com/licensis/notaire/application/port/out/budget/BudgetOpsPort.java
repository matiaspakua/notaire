package com.licensis.notaire.application.port.out.budget;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for budgets (issue #1805).
 * Exposes view shapes only so the application layer stays free of entities.
 */
public interface BudgetOpsPort {

    record PersonView(Integer personId, String name, String lastName) {}

    record BudgetView(
            Integer idBudget,
            int number,
            Date date,
            String heading,
            String status,
            BigDecimal propertyAmount,
            String notes,
            PersonView person,
            int version) {}

    record BudgetCommand(
            Integer number,
            Date date,
            String heading,
            String status,
            BigDecimal propertyAmount,
            String notes,
            Integer personId) {}

    record PageResult(List<BudgetView> content, int number, int size, long totalElements) {}

    sealed interface SaveResult {
        record Created(BudgetView view) implements SaveResult {}

        record Updated(BudgetView view) implements SaveResult {}

        record NotFound() implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}
    }

    PageResult findAll(int page, int size, String sort);

    Optional<BudgetView> findById(Integer id);

    List<BudgetView> findByPerson(Integer personId);

    List<BudgetView> findByStatus(String status);

    SaveResult create(BudgetCommand command);

    SaveResult update(Integer id, BudgetCommand command);

    DeleteResult delete(Integer id);

    /** Existence check for other application slices (issue #1865). */
    boolean existsById(Integer id);
}
