package com.licensis.notaire.domain.substitution;

/** Invalid substitution write (empty 400). */
public class SubstitutionBadRequestException extends SubstitutionException {

    public SubstitutionBadRequestException() {
        super("Invalid substitution request");
    }
}
