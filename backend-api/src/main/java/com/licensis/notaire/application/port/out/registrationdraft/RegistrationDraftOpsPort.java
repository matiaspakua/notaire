package com.licensis.notaire.application.port.out.registrationdraft;

import java.util.Date;
import java.util.Optional;

import com.licensis.notaire.dto.DtoRegistrationDraft;

/**
 * Outbound ops port for registration drafts (issue #1819 / CU82).
 * Returns DTO shapes only so inbound adapters stay free of entities.
 */
public interface RegistrationDraftOpsPort {

    sealed interface DraftResult {
        record Ok(DtoRegistrationDraft dto) implements DraftResult {}

        record NotFound() implements DraftResult {}

        record Invalid(String message) implements DraftResult {}
    }

    Optional<DtoRegistrationDraft> findById(Integer id);

    DraftResult generate(Integer idDeed);

    DraftResult submit(Integer id, Date dateSubmission, String registryEntryNumber);

    DraftResult observe(Integer id, String registryNotes, Date dateCorrection);

    DraftResult recordRegistration(Integer id, Date dateReception, String finalRegistrationNumber);
}
