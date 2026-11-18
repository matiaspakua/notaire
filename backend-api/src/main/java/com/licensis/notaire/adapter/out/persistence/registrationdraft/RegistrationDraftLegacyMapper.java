package com.licensis.notaire.adapter.out.persistence.registrationdraft;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.dto.DtoRegistrationDraft;

/**
 * Maps a legacy {@link RegistrationDraft} entity onto its transport DTO.
 * The entity no longer builds the DTO itself (#580).
 */
public final class RegistrationDraftLegacyMapper {

    private RegistrationDraftLegacyMapper() {
    }

    public static DtoRegistrationDraft toDto(RegistrationDraft draft) {
        Deed deed = draft.getFkIdDeed();
        return new DtoRegistrationDraft(
                draft.getIdRegistrationDraft(),
                draft.getNumber(),
                draft.getOperationPrice(),
                draft.getStatus(),
                draft.getDateGeneration(),
                draft.getDateSubmission(),
                draft.getRegistryEntryNumber(),
                draft.getDateReception(),
                draft.getFinalRegistrationNumber(),
                draft.getRegistryNotes(),
                draft.getDateCorrection(),
                deed != null ? deed.getIdDeed() : null);
    }
}
