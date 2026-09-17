package com.licensis.notaire.application.port.in.property;

import com.licensis.notaire.business.Property;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port for managing properties.
 */
public interface PropertyUseCase {

    List<Property> findAll();

    Optional<Property> findById(Integer id);

    Property save(Property entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
