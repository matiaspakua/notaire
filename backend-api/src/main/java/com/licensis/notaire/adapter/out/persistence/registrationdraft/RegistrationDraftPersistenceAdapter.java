package com.licensis.notaire.adapter.out.persistence.registrationdraft;

import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftRepositoryPort;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.repository.RegistrationDraftRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the registration draft slice.
 */
@Component
public class RegistrationDraftPersistenceAdapter implements RegistrationDraftRepositoryPort {

    private final RegistrationDraftRepository registrationDraftRepository;

    public RegistrationDraftPersistenceAdapter(RegistrationDraftRepository registrationDraftRepository) {
        this.registrationDraftRepository = registrationDraftRepository;
    }

    @Override
    public List<RegistrationDraft> findAll() {
        return registrationDraftRepository.findAll();
    }

    @Override
    public Optional<RegistrationDraft> findById(Integer id) {
        return registrationDraftRepository.findById(id);
    }

    @Override
    public RegistrationDraft save(RegistrationDraft entity) {
        return registrationDraftRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        registrationDraftRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return registrationDraftRepository.existsById(id);
    }
}
