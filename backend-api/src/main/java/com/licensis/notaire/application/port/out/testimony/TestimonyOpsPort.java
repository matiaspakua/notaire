package com.licensis.notaire.application.port.out.testimony;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoTestimony;

/**
 * Outbound persistence/ops port for testimonies (issue #1783).
 * Returns DTO shapes only so the application layer stays free of entities and Spring Data.
 */
public interface TestimonyOpsPort {

    /** Reason for a 409 on delete (issue #1364). */
    String IN_USE_MESSAGE = "Cannot delete: the testimony has registry movements or issued copies";

    sealed interface SaveResult {
        record Created(DtoTestimony dto) implements SaveResult {}

        record Updated() implements SaveResult {}

        record NotFound() implements SaveResult {}

        record StaleVersion() implements SaveResult {}

        /** Caught persistence failure — map with {@code ErrorResponses.createFailed/updateFailed}. */
        record Failed(Exception cause) implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record InUse(String message) implements DeleteResult {}

        record Conflict(String message) implements DeleteResult {}
    }

    List<DtoTestimony> findAll();

    Optional<DtoTestimony> findById(Integer id);

    SaveResult create(DtoTestimony dto);

    SaveResult update(Integer id, DtoTestimony dto);

    DeleteResult delete(Integer id);

    DtoTestimony generate(Integer idDeed);

    DtoTestimony verify(Integer id, boolean flagged, String notes);
}
