package com.licensis.notaire.domain.substitution;

/** Substitution or related aggregate not found (empty 404). */
public class SubstitutionNotFoundException extends SubstitutionException {

    public SubstitutionNotFoundException() {
        super("Substitution not found");
    }
}
