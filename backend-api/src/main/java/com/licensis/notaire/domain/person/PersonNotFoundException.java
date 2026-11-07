package com.licensis.notaire.domain.person;

/** No person exists for the requested id. */
public class PersonNotFoundException extends PersonException {

    public PersonNotFoundException() {
        super("Person not found");
    }
}
