package com.licensis.notaire.application.port.out.registrationdraft;

import com.licensis.notaire.business.RegistrationDraft;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for registration draft persistence operations.
 */
public interface RegistrationDraftRepositoryPort {

    List<RegistrationDraft> findAll();

    Optional<RegistrationDraft> findById(Integer id);

    RegistrationDraft save(RegistrationDraft entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
