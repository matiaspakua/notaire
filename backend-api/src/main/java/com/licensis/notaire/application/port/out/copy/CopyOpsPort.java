package com.licensis.notaire.application.port.out.copy;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for copies (issue #1779).
 * Returns view/command shapes only so the application layer stays free of entities and Spring Data.
 */
public interface CopyOpsPort {

    record CopyView(
            Integer idCopy,
            int number,
            Date datePrinting,
            Date dateWithdrawal,
            String notes,
            Integer personId,
            Integer testimonyId,
            int version) {}

    record CopyCommand(
            Integer number,
            Date datePrinting,
            Date dateWithdrawal,
            String notes,
            Integer personId,
            Integer testimonyId) {}

    sealed interface SaveResult {
        record Created(CopyView view) implements SaveResult {}

        record Updated() implements SaveResult {}

        record Conflict(String message) implements SaveResult {}

        record NotFound() implements SaveResult {}

        record ServerError() implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record Conflict() implements DeleteResult {}
    }

    List<CopyView> findAll();

    Optional<CopyView> findById(Integer id);

    SaveResult create(CopyCommand command);

    SaveResult update(Integer id, CopyCommand command);

    DeleteResult delete(Integer id);
}
