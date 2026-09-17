package com.licensis.notaire.application.port.out.property;

import com.licensis.notaire.business.Property;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for property persistence operations.
 */
public interface PropertyRepositoryPort {

    List<Property> findAll();

    Optional<Property> findById(Integer id);

    Property save(Property entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
