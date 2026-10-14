package com.licensis.notaire.application.port.out.property;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.property.Property;

/** Property storage in domain terms. The entity-returning {@code PropertyRepositoryPort} remains for legacy callers. */
public interface PropertyCatalogPort {

    List<Property> findAll();

    Optional<Property> findById(int id);

    /** Inserts when the property has no id, otherwise updates it. */
    Property save(Property property);

    /** @return false when no property had that id */
    boolean deleteById(int id);
}
