package com.licensis.notaire.adapter.out.persistence.registrationdraft;

import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftOpsPort;
import com.licensis.notaire.application.usecase.registrationdraft.RegistrationDraftService;
import com.licensis.notaire.dto.DtoRegistrationDraft;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;

/**
 * Ops adapter for registration drafts. Wraps {@link RegistrationDraftService} and maps
 * entities/exceptions to DTO sealed results for the inbound ports (issue #1819).
 */
@Component
public class RegistrationDraftOpsAdapter implements RegistrationDraftOpsPort {

    private final RegistrationDraftService service;

    public RegistrationDraftOpsAdapter(RegistrationDraftService service) {
        this.service = service;
    }

    @Override
    public Optional<DtoRegistrationDraft> findById(Integer id) {
        return service.findById(id).map(draft -> draft.getDto());
    }

    @Override
    public DraftResult generate(Integer idDeed) {
        try {
            return new DraftResult.Ok(service.generate(idDeed).getDto());
        } catch (ResourceNotFoundException e) {
            return new DraftResult.NotFound();
        } catch (BusinessValidationException e) {
            return new DraftResult.Invalid(e.getMessage());
        }
    }

    @Override
    public DraftResult submit(Integer id, Date dateSubmission, String registryEntryNumber) {
        try {
            return new DraftResult.Ok(service.submit(id, dateSubmission, registryEntryNumber).getDto());
        } catch (ResourceNotFoundException e) {
            return new DraftResult.NotFound();
        } catch (BusinessValidationException e) {
            return new DraftResult.Invalid(e.getMessage());
        }
    }

    @Override
    public DraftResult observe(Integer id, String registryNotes, Date dateCorrection) {
        try {
            return new DraftResult.Ok(service.observe(id, registryNotes, dateCorrection).getDto());
        } catch (ResourceNotFoundException e) {
            return new DraftResult.NotFound();
        } catch (BusinessValidationException e) {
            return new DraftResult.Invalid(e.getMessage());
        }
    }

    @Override
    public DraftResult recordRegistration(Integer id, Date dateReception, String finalRegistrationNumber) {
        try {
            return new DraftResult.Ok(
                    service.recordRegistration(id, dateReception, finalRegistrationNumber).getDto());
        } catch (ResourceNotFoundException e) {
            return new DraftResult.NotFound();
        } catch (BusinessValidationException e) {
            return new DraftResult.Invalid(e.getMessage());
        }
    }
}
