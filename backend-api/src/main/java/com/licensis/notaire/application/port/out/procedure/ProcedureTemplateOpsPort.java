package com.licensis.notaire.application.port.out.procedure;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for procedure templates (issue #1798).
 * Exposes view shapes only so the application layer stays free of entities.
 */
public interface ProcedureTemplateOpsPort {

    record TemplateView(Integer procedureTypeId, Integer documentTypeId, String notes, int version) {
    }

    sealed interface SaveResult {
        record Created(TemplateView view) implements SaveResult {}

        record Updated(TemplateView view) implements SaveResult {}

        record NotFound() implements SaveResult {}

        record BadRequest() implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}
    }

    List<TemplateView> findAll();

    List<TemplateView> findByProcedureType(Integer procedureTypeId);

    Optional<TemplateView> findById(Integer procedureTypeId, Integer documentTypeId);

    SaveResult create(Integer procedureTypeId, Integer documentTypeId, String notes);

    SaveResult update(Integer procedureTypeId, Integer documentTypeId, String notes);

    DeleteResult delete(Integer procedureTypeId, Integer documentTypeId);
}
