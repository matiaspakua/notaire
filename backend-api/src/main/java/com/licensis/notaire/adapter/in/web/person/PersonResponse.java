package com.licensis.notaire.adapter.in.web.person;

import java.util.Date;

import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonView;

import jakarta.validation.constraints.NotBlank;

/**
 * Wire DTO for people. Built only from {@link PersonView} so the inbound adapter stays ports-only
 * (issue #1813).
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
}
