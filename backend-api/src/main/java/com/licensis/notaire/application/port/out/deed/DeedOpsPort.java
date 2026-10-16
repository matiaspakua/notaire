package com.licensis.notaire.application.port.out.deed;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence/ops port for deeds (issue #1811).
 * Returns view/command shapes only so the application layer stays free of entities and Spring Data.
 */
public interface DeedOpsPort {

    record DeedView(
            Integer idDeed,
            int number,
            String body,
            String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            int version) {}

    record DeedCommand(
            Integer number,
            String body,
            String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            Integer idFolio) {}

    /** Wire-compatible notary person fields for GET /available-notaries. */
    record NotaryView(
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

    record PageResult(List<DeedView> content, int number, int size, long totalElements) {}

    sealed interface SaveResult {
        record Created(DeedView view) implements SaveResult {}

        record Updated(DeedView view) implements SaveResult {}

        record NotFound() implements SaveResult {}
    }

    sealed interface DeleteResult {
        record Deleted() implements DeleteResult {}

        record NotFound() implements DeleteResult {}
    }

    PageResult findAll(String status, int page, int size, String sort);

    Optional<DeedView> findById(Integer id);

    List<DeedView> searchByNumber(Integer number);

    List<NotaryView> findAvailableNotaries();

    SaveResult create(DeedCommand command);

    SaveResult update(Integer id, DeedCommand command);

    DeleteResult delete(Integer id);

    DeedView sign(Integer id);
}
