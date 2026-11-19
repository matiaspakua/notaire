package com.licensis.notaire.adapter.out.persistence.testimony;

import java.util.ArrayList;
import java.util.List;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoFolioType;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.dto.DtoTestimonyMovement;

/** Transport mapping for {@link Testimony}. The entity does not import these DTOs. */
public final class TestimonyLegacyMapper {

    private TestimonyLegacyMapper() {
    }

    public static DtoTestimony toDto(Testimony testimony) {
        DtoTestimony dto = new DtoTestimony();
        dto.setIdTestimony(testimony.getIdTestimony());
        dto.setNumber(testimony.getNumber());
        dto.setNotes(testimony.getNotes());
        dto.setFlagged(testimony.getFlagged());
        dto.setVerified(testimony.getVerified());
        dto.setVersion(testimony.getVersion());
        if (testimony.getFkIdDeed() != null) {
            dto.setDeed(copyDeed(testimony.getFkIdDeed()));
        }
        List<TestimonyMovement> movements = testimony.getTestimonyMovementList();
        if (movements != null && !movements.isEmpty()) {
            List<DtoTestimonyMovement> movementDtos = new ArrayList<>();
            for (TestimonyMovement movement : movements) {
                movementDtos.add(copyMovement(movement));
            }
            dto.setTestimonyMovements(movementDtos);
        }
        return dto;
    }

    public static void apply(Testimony testimony, DtoTestimony dto) {
        if (dto.getIdTestimony() != null) {
            testimony.setIdTestimony(dto.getIdTestimony());
        }
        testimony.setNumber(dto.getNumber());
        testimony.setNotes(dto.getNotes());
        testimony.setFlagged(dto.isFlagged());
        testimony.setVerified(dto.isVerified());
        testimony.setVersion(dto.getVersion());
        if (dto.getDeed() != null) {
            Deed deed = new Deed();
            deed.setIdDeed(dto.getDeed().getIdDeed());
            testimony.setFkIdDeed(deed);
        }
        if (dto.getTestimonyMovements() != null && !dto.getTestimonyMovements().isEmpty()) {
            List<TestimonyMovement> movements = new ArrayList<>();
            for (DtoTestimonyMovement movement : dto.getTestimonyMovements()) {
                movements.add(copyMovement(movement));
            }
            testimony.setTestimonyMovementList(movements);
        }
    }

    private static DtoDeed copyDeed(Deed deed) {
        DtoDeed deedDto = new DtoDeed();
        deedDto.setIdDeed(deed.getIdDeed());
        deedDto.setDateDeedrecording(deed.getDateDeedrecording());
        deedDto.setNumber(deed.getNumber());
        deedDto.setBody(deed.getBody());
        deedDto.setStatus(deed.getStatus());
        deedDto.setRegistrationEntryNumber(deed.getRegistrationEntryNumber());
        deedDto.setDateRegistration(deed.getDateRegistration());
        deedDto.setVersion(deed.getVersion());
        if (deed.getFolioList() != null && !deed.getFolioList().isEmpty()) {
            for (Folio folio : deed.getFolioList()) {
                deedDto.getFolios().add(copyFolio(folio));
            }
        } else {
            deedDto.setFolios(null);
        }
        if (deed.getProcedureList() != null && !deed.getProcedureList().isEmpty()) {
            for (Procedure procedure : deed.getProcedureList()) {
                deedDto.getProcedures().add(procedure.getDto());
            }
        } else {
            deedDto.setProcedures(null);
        }
        return deedDto;
    }

    private static DtoFolio copyFolio(Folio folio) {
        DtoFolio folioDto = new DtoFolio();
        folioDto.setIdFolio(folio.getIdFolio());
        folioDto.setNumber(folio.getNumber());
        folioDto.setStatus(folio.getStatus());
        folioDto.setNotes(folio.getNotes());
        folioDto.setYear(folio.getYear());
        folioDto.setVersion(folio.getVersion());
        if (folio.getFkIdNotaryPerson() != null) {
            DtoPerson person = new DtoPerson();
            person.setId(folio.getFkIdNotaryPerson().getPersonId());
            person.setNotaryRegistrationNumber(folio.getFkIdNotaryPerson().getNotaryRegistrationNumber());
            folioDto.setPersonNotary(person);
        }
        DtoFolioType folioTypeDto = new DtoFolioType();
        folioTypeDto.setIdFolioType(folio.getFkIdFolioType().getIdFolioType());
        folioTypeDto.setName(folio.getFkIdFolioType().getName());
        folioTypeDto.setNotes(folio.getFkIdFolioType().getNotes());
        folioTypeDto.setEnabled(folio.getFkIdFolioType().getEnabled());
        folioTypeDto.setIsAuxiliary(folio.getFkIdFolioType().isIsAuxiliary());
        folioTypeDto.setVersion(folio.getFkIdFolioType().getVersion());
        folioDto.setFolioType(folioTypeDto);
        if (folio.getFkIdDeed() != null) {
            DtoDeed nested = new DtoDeed();
            nested.setIdDeed(folio.getFkIdDeed().getIdDeed());
            nested.setNumber(folio.getFkIdDeed().getNumber());
            nested.setStatus(folio.getFkIdDeed().getStatus());
            folioDto.setDeed(nested);
        }
        return folioDto;
    }

    private static TestimonyMovement copyMovement(DtoTestimonyMovement dto) {
        TestimonyMovement movement = new TestimonyMovement();
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
        return movement;
    }

    private static DtoTestimonyMovement copyMovement(TestimonyMovement movement) {
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
        DtoTestimony testimonyDto = new DtoTestimony();
        testimonyDto.setIdTestimony(movement.getTestimony().getIdTestimony());
        dto.setTestimony(testimonyDto);
        return dto;
    }
}
