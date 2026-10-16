package com.licensis.notaire.application.port.out.procedure;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoProcedureType;

/**
 * Outbound persistence/ops port for procedure types (issue #1791).
 * Returns DTO shapes only so the application layer stays free of entities and Spring Data.
 */
public interface ProcedureTypeOpsPort {

    String IN_USE_MODIFY_MESSAGE =
            "This procedure type is in use and cannot be modified. Create a new one.";
    String IN_USE_DELETE_MESSAGE =
            "Cannot delete: the procedure type is in use on templates or procedures.";
    String WORKFLOW_INACTIVE_MESSAGE = "The selected workflow is not active.";

    sealed interface SaveResult {
        record Created(DtoProcedureType dto) implements SaveResult {}

        record Updated() implements SaveResult {}

        record NotFound() implements SaveResult {}

        record InUse(String message) implements SaveResult {}

        record Failed(Exception cause) implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record InUse(String message) implements DeleteResult {}
    }

    sealed interface WorkflowAssignResult {
        record Ok(DtoProcedureType dto) implements WorkflowAssignResult {}

        record NotFound() implements WorkflowAssignResult {}

        record Inactive() implements WorkflowAssignResult {}
    }

    List<DtoProcedureType> findAll();

    List<DtoProcedureType> searchByName(String name);

    Optional<DtoProcedureType> findById(Integer id);

    /** Empty when the type does not exist. */
    Optional<Boolean> isInUse(Integer id);

    SaveResult create(DtoProcedureType dto);

    SaveResult update(Integer id, DtoProcedureType dto);

    DeleteResult delete(Integer id);

    /**
     * @param workflowDefinitionId {@code null} unassigns; non-null assigns that active workflow
     */
    WorkflowAssignResult assignWorkflow(Integer id, Integer workflowDefinitionId);
}
