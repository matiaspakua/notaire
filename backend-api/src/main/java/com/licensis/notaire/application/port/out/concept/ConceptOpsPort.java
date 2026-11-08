package com.licensis.notaire.application.port.out.concept;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoConcept;

/**
 * Outbound persistence/ops port for concepts (issue #1815).
 */
public interface ConceptOpsPort {

    sealed interface SaveResult {
        record Created(DtoConcept dto) implements SaveResult {}

        record Updated() implements SaveResult {}

        record NotFound() implements SaveResult {}

        record InUse(String error) implements SaveResult {}

        record Failed(Exception cause, boolean create) implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record InUse(String error) implements DeleteResult {}

        record Conflict(Exception cause) implements DeleteResult {}
    }

    sealed interface InUseResult {
        record Ok(boolean inUse) implements InUseResult {}

        record NotFound() implements InUseResult {}
    }

    List<DtoConcept> findAll();

    List<DtoConcept> searchByName(String name);

    Optional<DtoConcept> findById(Integer id);

    InUseResult isInUse(Integer id);

    SaveResult create(DtoConcept dto);

    SaveResult update(Integer id, DtoConcept dto);

    DeleteResult delete(Integer id);
}
