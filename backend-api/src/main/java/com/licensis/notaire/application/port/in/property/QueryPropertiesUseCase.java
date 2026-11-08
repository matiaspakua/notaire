package com.licensis.notaire.application.port.in.property;

import java.util.List;

import com.licensis.notaire.domain.property.Property;

public interface QueryPropertiesUseCase {

    List<Property> findAll();

    /** @throws com.licensis.notaire.domain.property.PropertyNotFoundException when absent */
    Property getById(int id);
}
