package com.licensis.notaire.domain.person;

/** Person cannot be deleted because another aggregate references it (plain-text 409 body). */
public class PersonInUseException extends PersonException {

    public PersonInUseException(String message) {
        super(message);
    }
}
