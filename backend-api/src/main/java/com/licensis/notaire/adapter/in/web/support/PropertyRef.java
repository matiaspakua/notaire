package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.Property;

public record PropertyRef(Integer idProperty, String cadastralDesignation, String address,
        String registrationNumber) {

    public static PropertyRef from(Property property) {
        return property == null ? null : new PropertyRef(property.getIdProperty(),
                property.getCadastralDesignation(), property.getAddress(), property.getRegistrationNumber());
    }
}
