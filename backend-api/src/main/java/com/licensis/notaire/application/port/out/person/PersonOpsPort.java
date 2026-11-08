package com.licensis.notaire.application.port.out.person;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for people (issue #1813).
 * Returns view/command shapes only so the application layer stays free of entities and Spring Data.
 */
public interface PersonOpsPort {

    record PersonView(
            Integer personId,
            String firstName,
            String lastName,
            String identificationNumber,
            boolean isClient,
            String nationality,
            String taxId,
            String sex,
            Date birthDate,
            String maritalStatus,
            Integer marriageCount,
            String occupation,
            String address,
            String phone,
            String email,
            Integer notaryRegistrationNumber,
            Integer identificationTypeId,
            int version) {}

    record PersonCommand(
            String firstName,
            String lastName,
            String identificationNumber,
            boolean isClient,
            String nationality,
            String taxId,
            String sex,
            Date birthDate,
            String maritalStatus,
            Integer marriageCount,
            String occupation,
            String address,
            String phone,
            String email,
            Integer notaryRegistrationNumber,
            Integer identificationTypeId,
            Integer nestedIdentificationTypeId) {}

    record PageResult(List<PersonView> content, int number, int size, long totalElements) {}

    sealed interface SaveResult {
        record Created(PersonView view) implements SaveResult {}

        record Updated(PersonView view) implements SaveResult {}

        record NotFound() implements SaveResult {}

        record Duplicate(String message, Integer existingPersonId) implements SaveResult {}

        /** Constraint / unexpected failure; {@code create} vs {@code update} select the HTTP mapping. */
        record Failed(Exception cause, boolean create) implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}

        record Conflict(String message) implements DeleteResult {}
    }

    PageResult findAll(int page, int size, String sort);

    Optional<PersonView> findById(Integer id);

    List<PersonView> search(
            String firstName,
            String lastName,
            String identificationNumber,
            Integer idIdentificationType,
            Boolean isClient);

    SaveResult create(PersonCommand command);

    SaveResult update(Integer id, PersonCommand command);

    DeleteResult delete(Integer id);
}
