package com.licensis.notaire.application.port.out.history;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoHistorySummary;

/**
 * Outbound persistence/ops port for management history (issue #1775).
 * Returns DTO/view shapes only so the application layer stays free of entities and Spring Data.
 */
public interface HistoryOpsPort {

    record HistoryCommand(Date date, String notes, Integer managementStatusId, Integer managementId) {}

    record PageResult(List<DtoHistorySummary> content, int number, int size, long totalElements) {}

    sealed interface SaveResult {
        record Created(DtoHistorySummary summary) implements SaveResult {}

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

    PageResult findAll(int page, int size, String sort);

    Optional<DtoHistorySummary> findById(Integer id);

    List<DtoHistorySummary> findByManagementId(Integer idManagement);

    SaveResult create(HistoryCommand command);

    SaveResult update(Integer id, HistoryCommand command);

    DeleteResult delete(Integer id);
}
