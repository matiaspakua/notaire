package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.Person;

public record NotaryRef(Integer personId, Integer notaryRegistrationNumber) {

    public static NotaryRef from(Person notary) {
        return notary == null ? null : new NotaryRef(notary.getPersonId(), notary.getNotaryRegistrationNumber());
    }
}
