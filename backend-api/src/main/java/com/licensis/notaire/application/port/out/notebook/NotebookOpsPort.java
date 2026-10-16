package com.licensis.notaire.application.port.out.notebook;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for notebooks (issue #1817).
 */
public interface NotebookOpsPort {

    record NotaryView(
            Integer personId,
            String firstName,
            String lastName,
            String identificationNumber,
            Integer notaryRegistrationNumber) {}

    record NotebookView(
            Integer idNotebook, int number, int year, String notes, NotaryView notary, int version) {}

    sealed interface CreateResult {
        record Created(NotebookView view) implements CreateResult {}

        record BadRequest(String message) implements CreateResult {}

        record NotFound(String message) implements CreateResult {}
    }

    List<NotebookView> findAll();

    Optional<NotebookView> findById(Integer id);

    CreateResult create(List<Integer> idsFolio, Integer idNotary, int year, String notes);
}
