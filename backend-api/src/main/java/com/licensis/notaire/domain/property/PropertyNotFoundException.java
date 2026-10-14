package com.licensis.notaire.domain.property;

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(int id) {
        super("Property not found with ID: " + id);
    }
}
