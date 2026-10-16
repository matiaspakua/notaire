package com.licensis.notaire.application.port.out.procedure;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for procedures (issue #1801).
 * Exposes view shapes only so the application layer stays free of entities and Spring Data.
 */
public interface ProcedureOpsPort {

    record ProcedureTypeView(
            Integer idProcedureType,
            String name,
            String notes,
            boolean isArchived,
            boolean isRegistered,
            boolean associatesProperties,
            boolean enabled) {}

    record PropertyView(
            Integer idProperty,
            String cadastralDesignation,
            String address,
            String registrationNumber) {}

    record DeedView(Integer idDeed, int number, String status) {}

    record ManagementView(Integer idManagement, int number, String heading) {}

    record BudgetView(Integer idBudget) {}

    record ProcedureView(
            Integer idProcedure,
            String notes,
            ProcedureTypeView procedureType,
            PropertyView property,
            BudgetView budget,
            DeedView deed,
            ManagementView management,
            int version) {}

    record ProcedureCommand(
            Integer idProcedureType,
            Integer idProperty,
            Integer idDeed,
            Integer idManagement,
            Integer idBudget,
            String notes) {}

    record PageResult(List<ProcedureView> content, int number, int size, long totalElements) {}

    sealed interface SaveResult {
        record Created(ProcedureView view) implements SaveResult {}

        record Updated(ProcedureView view) implements SaveResult {}

        record NotFound() implements SaveResult {}

        record ServerError() implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record Conflict() implements DeleteResult {}
    }

    PageResult findAll(int page, int size, String sort);

    Optional<ProcedureView> findById(Integer id);

    SaveResult create(ProcedureCommand command);

    SaveResult update(Integer id, ProcedureCommand command);

    DeleteResult delete(Integer id);
}
