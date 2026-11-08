package com.licensis.notaire.application.port.out.testimony;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.usecase.testimony.TestimonyReentry;
import com.licensis.notaire.dto.DtoTestimonyMovement;

/**
 * Outbound persistence/ops port for testimony movements (issue #1787).
 * Returns DTO shapes only so the application layer stays free of entities and Spring Data.
 */
public interface TestimonyMovementOpsPort {

    sealed interface SaveResult {
        record Created(DtoTestimonyMovement dto) implements SaveResult {}

        record Updated() implements SaveResult {}

        record NotFound() implements SaveResult {}

        record StaleVersion() implements SaveResult {}

        /** Caught persistence failure — map with {@code ErrorResponses.createFailed/updateFailed}. */
        record Failed(Exception cause) implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record Conflict(String message) implements DeleteResult {}
    }

    List<DtoTestimonyMovement> findAll();

    Optional<DtoTestimonyMovement> findById(Integer id);

    SaveResult create(DtoTestimonyMovement dto);

    SaveResult update(Integer id, DtoTestimonyMovement dto);

    DeleteResult delete(Integer id);

    DtoTestimonyMovement registerEntry(Integer idTestimony);

    DtoTestimonyMovement registerInscription(Integer idTestimony);

    DtoTestimonyMovement withdraw(Integer idTestimony, int cardNumber);

    DtoTestimonyMovement reenter(Integer idTestimony, TestimonyReentry reentry);
}
