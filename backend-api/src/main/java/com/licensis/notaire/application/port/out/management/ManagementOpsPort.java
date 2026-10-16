package com.licensis.notaire.application.port.out.management;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.dto.DtoManagementCaseSummary;
import com.licensis.notaire.dto.DtoManagementFinancialSummary;
import com.licensis.notaire.dto.DtoManagementSummary;

/**
 * Outbound persistence/ops port for deed managements (issue #1772).
 * Returns DTO/view shapes only so the application layer stays free of entities and Spring Data.
 */
public interface ManagementOpsPort {

    record ManagementView(
            Integer idManagement,
            int number,
            String heading,
            Date dateStart,
            String notes,
            Integer notaryPersonId,
            Integer managementStatusId,
            Boolean pendingDebtAtArchiving,
            int version) {}

    record CompleteCaseView(
            Integer idManagement,
            Integer number,
            String heading,
            String notes,
            Integer budgetId,
            Integer notaryId,
            Integer statusManagementId,
            Integer typeProcedureId,
            Integer propertyId) {}

    record CompleteCaseCommand(
            Integer number,
            String heading,
            String notes,
            Integer budgetId,
            Integer notaryId,
            Integer statusManagementId,
            Integer typeProcedureId,
            Integer propertyId) {}

    record ManagementCommand(
            Integer number,
            String heading,
            Date dateStart,
            String notes,
            Integer notaryPersonId,
            Integer managementStatusId,
            Boolean pendingDebtAtArchiving) {}

    record ArchivedManagementView(
            Integer idManagement,
            BigDecimal pendingBalance,
            boolean pendingDebtAtArchiving) {}

    record PageResult(List<DtoManagementSummary> content, int number, int size, long totalElements) {}

    sealed interface CompleteCaseResult {
        record Created(DtoManagementSummary summary) implements CompleteCaseResult {}
        record Updated(DtoManagementSummary summary) implements CompleteCaseResult {}
        record BadRequest() implements CompleteCaseResult {}
        record NotFound() implements CompleteCaseResult {}
    }

    sealed interface SaveResult {
        record Created(ManagementView view) implements SaveResult {}
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

    sealed interface ArchiveResult {
        record Ok(ArchivedManagementView view) implements ArchiveResult {}
        record NotFound() implements ArchiveResult {}
        record WaitingFolders(String message, List<Integer> waitingFolderNumbers) implements ArchiveResult {}
    }

    PageResult findAll(int page, int size, String sort);

    Optional<DtoManagementSummary> findById(Integer id);

    Optional<DtoManagementSummary> findByNumber(int number);

    List<ManagementView> findByClientPersonId(Integer idPerson);

    Optional<DtoHistorySummary> findCurrentStatus(Integer id);

    Optional<CompleteCaseView> findCompleteCase(Integer id);

    Optional<List<DtoHistorySummary>> findHistory(Integer id);

    Optional<String> findCurrentStatusName(Integer id);

    CompleteCaseResult createCompleteCase(CompleteCaseCommand command);

    CompleteCaseResult updateCompleteCase(Integer id, CompleteCaseCommand command);

    SaveResult create(ManagementCommand command);

    SaveResult update(Integer id, ManagementCommand command);

    DeleteResult delete(Integer id);

    ArchiveResult archive(Integer id, boolean confirmed);

    DtoManagementCaseSummary getCaseSummary(Integer id);

    DtoManagementFinancialSummary getFinancialSummary(Integer id);

    BigDecimal calculatePendingBalance(Integer id);
}
