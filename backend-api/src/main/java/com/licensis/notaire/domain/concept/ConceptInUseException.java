package com.licensis.notaire.domain.concept;

/** Concept is locked because another aggregate references it. */
public class ConceptInUseException extends ConceptException {

    public ConceptInUseException(String message) {
        super(message);
    }
}
