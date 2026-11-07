package com.licensis.notaire.domain.testimony;

/** No testimony (or linked deed) exists for the requested id. */
public class TestimonyNotFoundException extends TestimonyException {

    public TestimonyNotFoundException() {
        super("Testimony not found");
    }
}
