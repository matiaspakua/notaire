package com.licensis.notaire.testing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.licensis.notaire.application.port.out.property.PropertyCatalogPort;
import com.licensis.notaire.domain.property.Property;

/** In-memory adapter for the property catalog port: the use cases need no Spring or database. */
public class InMemoryPropertyCatalog implements PropertyCatalogPort {

    private final Map<Integer, Property> rows = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public List<Property> findAll() {
        return new ArrayList<>(rows.values());
    }

    @Override
    public Optional<Property> findById(int id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public Property save(Property property) {
        int id = property.id() != null ? property.id() : nextId++;
        int version = property.id() != null ? property.version() + 1 : 0;
        Property saved = property.withIdentity(id, version);
        rows.put(id, saved);
        return saved;
    }

    @Override
    public boolean deleteById(int id) {
        return rows.remove(id) != null;
    }
}
