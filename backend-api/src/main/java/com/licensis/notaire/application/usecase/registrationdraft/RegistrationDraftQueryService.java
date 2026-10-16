package com.licensis.notaire.application.usecase.registrationdraft;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.registrationdraft.QueryRegistrationDraftsUseCase;
import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftOpsPort;
import com.licensis.notaire.dto.DtoRegistrationDraft;

/**
 * Query-side use case for registration drafts — delegates to {@link RegistrationDraftOpsPort}
 * (issue #1819).
 */
@Service
public class RegistrationDraftQueryService implements QueryRegistrationDraftsUseCase {

    private final RegistrationDraftOpsPort ops;

    public RegistrationDraftQueryService(RegistrationDraftOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public Optional<DtoRegistrationDraft> findById(Integer id) {
        return ops.findById(id);
    }
}
