package com.licensis.notaire.testing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeUsagePort;
import com.licensis.notaire.domain.folio.FolioType;

/** In-memory adapter for the folio type ports: the use cases need no Spring or database. */
public class InMemoryFolioTypeCatalog implements FolioTypeCatalogPort, FolioTypeUsagePort {

    private final Map<Integer, FolioType> rows = new LinkedHashMap<>();
    private final Set<Integer> usedByFolios = new java.util.HashSet<>();
    private int nextId = 1;

    public InMemoryFolioTypeCatalog withUsedByFolios(int id) {
        usedByFolios.add(id);
        return this;
    }

    @Override
    public List<FolioType> findAll() {
        return new ArrayList<>(rows.values());
    }

    @Override
    public List<FolioType> findByNameContaining(String text) {
        return rows.values().stream().filter(type -> type.name() != null && type.name().contains(text)).toList();
    }

    @Override
    public Optional<FolioType> findById(int id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public FolioType save(FolioType type) {
        int id = type.id() != null ? type.id() : nextId++;
        int version = type.id() != null ? type.version() + 1 : 0;
        FolioType saved = type.withIdentity(id, version);
        rows.put(id, saved);
        return saved;
    }

    @Override
    public boolean deleteById(int id) {
        return rows.remove(id) != null;
    }

    @Override
    public boolean isUsedByFolios(int id) {
        return usedByFolios.contains(id);
    }
}
