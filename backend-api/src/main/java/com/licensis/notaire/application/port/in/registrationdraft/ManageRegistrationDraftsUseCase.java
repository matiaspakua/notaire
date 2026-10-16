package com.licensis.notaire.application.port.in.registrationdraft;

import java.util.Date;

import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftOpsPort.DraftResult;

/** Write-side inbound port for registration drafts (issue #1819 / CU82). */
public interface ManageRegistrationDraftsUseCase {

    DraftResult generate(Integer idDeed);

    DraftResult submit(Integer id, Date dateSubmission, String registryEntryNumber);

    DraftResult observe(Integer id, String registryNotes, Date dateCorrection);

    DraftResult recordRegistration(Integer id, Date dateReception, String finalRegistrationNumber);
}
