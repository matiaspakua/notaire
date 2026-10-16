package com.licensis.notaire.adapter.in.web.person;

import java.util.Date;

import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonView;
import com.licensis.notaire.business.Person;

import jakarta.validation.constraints.NotBlank;

/**
 * Wire DTO for people. {@link #from(Person)} remains for DeedController available-notaries until
 * that surface is ports-only (issue #1813).
 */
public record PersonResponse(
        Integer personId,
        String firstName,
        @NotBlank String lastName,
        @NotBlank String identificationNumber,
        boolean isClient,
        String nationality,
        String taxId,
        String sex,
        Date birthDate,
        String maritalStatus,
        Integer marriageCount,
        String occupation,
        String address,
        String phone,
        String email,
        Integer notaryRegistrationNumber,
        Integer identificationTypeId,
        int version) {

    public static PersonResponse from(PersonView view) {
        return new PersonResponse(
                view.personId(),
                view.firstName(),
                view.lastName(),
                view.identificationNumber(),
                view.isClient(),
                view.nationality(),
                view.taxId(),
                view.sex(),
                view.birthDate(),
                view.maritalStatus(),
                view.marriageCount(),
                view.occupation(),
                view.address(),
                view.phone(),
                view.email(),
                view.notaryRegistrationNumber(),
                view.identificationTypeId(),
                view.version());
    }

    public static PersonResponse from(Person person) {
        Integer typeId = person.getFkIdIdentificationType() != null
                ? person.getFkIdIdentificationType().getIdIdentificationType()
                : null;
        return new PersonResponse(
                person.getPersonId(),
                person.getFirstName(),
                person.getLastName(),
                person.getIdentificationNumber(),
                person.getIsClient(),
                person.getNationality(),
                person.getTaxId(),
                person.getSex(),
                person.getBirthDate(),
                person.getMaritalStatus(),
                person.getMarriageCount(),
                person.getOccupation(),
                person.getAddress(),
                person.getPhone(),
                person.getEmail(),
                person.getNotaryRegistrationNumber(),
                typeId,
                person.getVersion());
    }
}
