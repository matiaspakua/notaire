package com.licensis.notaire.adapter.out.persistence.testimony;

import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.dto.DtoTestimonyMovement;

/**
 * Maps a legacy {@link TestimonyMovement} onto its transport DTO.
 * The entity no longer builds or applies the DTO itself (#580).
 */
public final class TestimonyMovementLegacyMapper {

    private TestimonyMovementLegacyMapper() {
    }

    public static DtoTestimonyMovement toDto(TestimonyMovement movement) {
        DtoTestimonyMovement dto = new DtoTestimonyMovement();
        dto.setIdTestimonyMovement(movement.getIdTestimonyMovement());
        dto.setDateEntry(movement.getDateEntry());
        dto.setDateRegistration(movement.getDateRegistration());
        dto.setDateExit(movement.getDateExit());
        dto.setRegistered(movement.getRegistered());
        dto.setCardNumber(movement.getCardNumber());
        dto.setNotes(movement.getNotes());
        dto.setObservedByRegistry(movement.isObservedByRegistry());
        dto.setVersion(movement.getVersion());
        Testimony testimony = movement.getTestimony();
        DtoTestimony testimonyDto = new DtoTestimony();
        testimonyDto.setIdTestimony(testimony.getIdTestimony());
        dto.setTestimony(testimonyDto);
        return dto;
    }

    public static void apply(TestimonyMovement movement, DtoTestimonyMovement dto) {
        if (dto.getIdTestimonyMovement() != null) {
            movement.setIdTestimonyMovement(dto.getIdTestimonyMovement());
        }
        movement.setDateEntry(dto.getDateEntry());
        movement.setDateRegistration(dto.getDateRegistration());
        movement.setDateExit(dto.getDateExit());
        movement.setRegistered(dto.isRegistered());
        movement.setCardNumber(dto.getCardNumber());
        movement.setNotes(dto.getNotes());
        movement.setObservedByRegistry(dto.isObservedByRegistry());
        movement.setVersion(dto.getVersion());
        if (dto.getTestimony() != null) {
            movement.setTestimony(new Testimony(dto.getTestimony().getIdTestimony()));
        }
    }
}
