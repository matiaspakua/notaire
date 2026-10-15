package com.licensis.notaire.testing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.management.ManagementStatusUsagePort;
import com.licensis.notaire.domain.management.ManagementStatus;

/** In-memory adapter for the management status ports: the use cases need no Spring or database. */
public class InMemoryManagementStatusCatalog implements ManagementStatusCatalogPort, ManagementStatusUsagePort {

    private final Map<Integer, ManagementStatus> rows = new LinkedHashMap<>();
    private final Set<Integer> inUse = new HashSet<>();
    private int nextId = 1;

    public InMemoryManagementStatusCatalog withInUse(int id) {
        inUse.add(id);
        return this;
    }

    @Override
    public List<ManagementStatus> findAll() {
        return new ArrayList<>(rows.values());
    }

    @Override
    public List<ManagementStatus> findByNameContaining(String text) {
        return rows.values().stream()
                .filter(status -> status.name() != null && status.name().contains(text))
                .toList();
    }

    @Override
    public Optional<ManagementStatus> findById(int id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public ManagementStatus save(ManagementStatus status) {
        int id = status.id() != null ? status.id() : nextId++;
        int version = status.id() != null ? status.version() + 1 : 0;
        ManagementStatus saved = status.withIdentity(id, version);
        rows.put(id, saved);
        return saved;
    }

    @Override
    public boolean deleteById(int id) {
        return rows.remove(id) != null;
    }

    @Override
    public boolean isUsedByDeedManagements(int id) {
        return inUse.contains(id);
    }
}
