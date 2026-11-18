package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.registrationdraft.RegistrationDraftLegacyMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.dto.DtoRegistrationDraft;

@DisplayName("Registration draft mapping lives outside the entity")
class RegistrationDraftLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyDraftFieldsOntoTheDto")
    void shouldCopyDraftFieldsOntoTheDto() {
        Deed deed = new Deed();
        deed.setIdDeed(4);
        Date generated = new Date(1_700_000_000_000L);
        Date submitted = new Date(1_700_100_000_000L);
        Date received = new Date(1_700_200_000_000L);
        Date corrected = new Date(1_700_300_000_000L);

        RegistrationDraft draft = new RegistrationDraft();
        draft.setIdRegistrationDraft(8);
        draft.setNumber(12);
        draft.setOperationPrice(new BigDecimal("250000"));
        draft.setStatus("OBSERVED");
        draft.setDateGeneration(generated);
        draft.setDateSubmission(submitted);
        draft.setRegistryEntryNumber("E-1");
        draft.setDateReception(received);
        draft.setFinalRegistrationNumber("F-1");
        draft.setRegistryNotes("fix the boundary");
        draft.setDateCorrection(corrected);
        draft.setFkIdDeed(deed);

        DtoRegistrationDraft dto = RegistrationDraftLegacyMapper.toDto(draft);

        assertThat(dto.getIdRegistrationDraft()).isEqualTo(8);
        assertThat(dto.getNumber()).isEqualTo(12);
        assertThat(dto.getOperationPrice()).isEqualByComparingTo("250000");
        assertThat(dto.getStatus()).isEqualTo("OBSERVED");
        assertThat(dto.getDateGeneration()).isEqualTo(generated);
        assertThat(dto.getDateSubmission()).isEqualTo(submitted);
        assertThat(dto.getRegistryEntryNumber()).isEqualTo("E-1");
        assertThat(dto.getDateReception()).isEqualTo(received);
        assertThat(dto.getFinalRegistrationNumber()).isEqualTo("F-1");
        assertThat(dto.getRegistryNotes()).isEqualTo("fix the boundary");
        assertThat(dto.getDateCorrection()).isEqualTo(corrected);
        assertThat(dto.getIdDeed()).isEqualTo(4);
    }

    @Test
    @DisplayName("shouldLeaveTheDeedIdEmptyWhenTheDraftHasNoDeed")
    void shouldLeaveTheDeedIdEmptyWhenTheDraftHasNoDeed() {
        RegistrationDraft draft = new RegistrationDraft();
        draft.setIdRegistrationDraft(1);

        DtoRegistrationDraft dto = RegistrationDraftLegacyMapper.toDto(draft);

        assertThat(dto.getIdDeed()).isNull();
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfRegistrationDraft")
    void shouldKeepDtoTypesOutOfRegistrationDraft() {
        assertThat(Arrays.stream(RegistrationDraft.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto");
    }
}
