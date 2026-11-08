package com.licensis.notaire.application.port.out.substitution;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for substitutions (issue #1807).
 * Returns view/command shapes only so the application layer stays free of entities and Spring Data.
 */
public interface SubstitutionOpsPort {

    record PersonView(Integer personId, String name, String lastName) {}

    record SubstitutionView(
            Integer idSubstitution,
            Date dateStart,
            Date dateEnd,
            String notes,
            Integer substitutePersonId,
            Integer substitutedPersonId,
            PersonView substitute,
            PersonView substituted,
            int version) {}

    record SubstitutionCommand(
            Date dateStart,
            Date dateEnd,
            String notes,
            Integer substitutePersonId,
            Integer substitutedPersonId) {}

    sealed interface SaveResult {
        record Created(SubstitutionView view) implements SaveResult {}

        record Updated() implements SaveResult {}

        record BadRequest() implements SaveResult {}

        record NotFound() implements SaveResult {}

        record ServerError() implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record ServerError() implements DeleteResult {}
    }

    List<SubstitutionView> findAll();

    Optional<SubstitutionView> findById(Integer id);

    SaveResult create(SubstitutionCommand command);

    SaveResult update(Integer id, SubstitutionCommand command);

    DeleteResult delete(Integer id);
}
