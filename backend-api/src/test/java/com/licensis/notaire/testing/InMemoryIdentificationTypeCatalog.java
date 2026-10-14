package com.licensis.notaire.testing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeUsagePort;
import com.licensis.notaire.domain.identification.IdentificationType;

/** In-memory adapter for the identification type ports: proves the use cases need no Spring or database. */
public class InMemoryIdentificationTypeCatalog implements IdentificationTypeCatalogPort, IdentificationTypeUsagePort {

    private final Map<Integer, IdentificationType> rows = new LinkedHashMap<>();
    private final Set<Integer> usedByPeople = new java.util.HashSet<>();
    private int nextId = 1;

    public InMemoryIdentificationTypeCatalog withUsedByPeople(int id) {
        usedByPeople.add(id);
        return this;
    }

    @Override
    public List<IdentificationType> findAll() {
        return new ArrayList<>(rows.values());
    }

    @Override
    public Optional<IdentificationType> findById(int id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public boolean existsById(int id) {
        return rows.containsKey(id);
    }

    @Override
    public IdentificationType save(IdentificationType type) {
        int id = type.id() != null ? type.id() : nextId++;
        int version = type.id() != null ? type.version() + 1 : 0;
        IdentificationType saved = IdentificationType.restore(id, type.name(), type.characters(), version);
        rows.put(id, saved);
        return saved;
    }

    @Override
    public void deleteById(int id) {
        rows.remove(id);
    }

    @Override
    public boolean isUsedByPeople(int id) {
        return usedByPeople.contains(id);
    }
}
