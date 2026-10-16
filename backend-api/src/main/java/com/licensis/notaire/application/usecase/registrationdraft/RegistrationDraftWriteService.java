package com.licensis.notaire.application.usecase.registrationdraft;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.registrationdraft.ManageRegistrationDraftsUseCase;
import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftOpsPort;
import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftOpsPort.DraftResult;

/**
 * Write-side use case for registration drafts — delegates to {@link RegistrationDraftOpsPort}
 * (issue #1819).
 */
@Service
public class RegistrationDraftWriteService implements ManageRegistrationDraftsUseCase {

    private final RegistrationDraftOpsPort ops;

    public RegistrationDraftWriteService(RegistrationDraftOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public DraftResult generate(Integer idDeed) {
        return ops.generate(idDeed);
    }

    @Override
    public DraftResult submit(Integer id, Date dateSubmission, String registryEntryNumber) {
        return ops.submit(id, dateSubmission, registryEntryNumber);
    }

    @Override
    public DraftResult observe(Integer id, String registryNotes, Date dateCorrection) {
        return ops.observe(id, registryNotes, dateCorrection);
    }

    @Override
    public DraftResult recordRegistration(Integer id, Date dateReception, String finalRegistrationNumber) {
        return ops.recordRegistration(id, dateReception, finalRegistrationNumber);
    }
}
