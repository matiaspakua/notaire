package com.licensis.notaire.domain.person;

/** Identification already belongs to another person (409 with existingPersonId). */
public class PersonDuplicateException extends PersonException {

    private final Integer existingPersonId;

    public PersonDuplicateException(String message, Integer existingPersonId) {
        super(message);
        this.existingPersonId = existingPersonId;
    }

    public Integer existingPersonId() {
        return existingPersonId;
    }
}
