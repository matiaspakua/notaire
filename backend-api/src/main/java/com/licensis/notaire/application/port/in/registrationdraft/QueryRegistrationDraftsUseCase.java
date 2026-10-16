package com.licensis.notaire.application.port.in.registrationdraft;

import java.util.Optional;

import com.licensis.notaire.dto.DtoRegistrationDraft;

/** Query-side inbound port for registration drafts (issue #1819). */
public interface QueryRegistrationDraftsUseCase {

    Optional<DtoRegistrationDraft> findById(Integer id);
}
