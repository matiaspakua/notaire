package com.licensis.notaire.domain.concept;

/** No concept exists for the requested id. */
public class ConceptNotFoundException extends ConceptException {

    public ConceptNotFoundException() {
        super("Concept not found");
    }
}
