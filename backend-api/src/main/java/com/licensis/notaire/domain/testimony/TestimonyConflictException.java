package com.licensis.notaire.domain.testimony;

/** Testimony cannot be deleted; plain-text 409 body (movements, copies, or other refs). */
public class TestimonyConflictException extends TestimonyException {

    public TestimonyConflictException(String message) {
        super(message);
    }
}
