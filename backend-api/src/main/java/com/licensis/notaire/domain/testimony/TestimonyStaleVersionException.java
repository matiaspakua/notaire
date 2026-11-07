package com.licensis.notaire.domain.testimony;

/** Optimistic-lock mismatch; mapped via {@code ErrorResponses.staleVersion}. */
public class TestimonyStaleVersionException extends TestimonyException {

    public TestimonyStaleVersionException() {
        super("Stale version");
    }
}
