package com.licensis.notaire.domain.testimony;

/** Impossible SaveResult arm; mapped to empty HTTP 500. */
public class TestimonyUnexpectedResultException extends TestimonyException {

    public TestimonyUnexpectedResultException(String message) {
        super(message);
    }
}
