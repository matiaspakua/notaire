package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.Person;
import jakarta.validation.constraints.NotBlank;

public record NotaryRef(Integer personId, String firstName, @NotBlank String lastName,
        @NotBlank String identificationNumber, Integer notaryRegistrationNumber) {

    public static NotaryRef from(Person notary) {
        return notary == null ? null : new NotaryRef(notary.getPersonId(), notary.getFirstName(),
                notary.getLastName(), notary.getIdentificationNumber(), notary.getNotaryRegistrationNumber());
    }
}
