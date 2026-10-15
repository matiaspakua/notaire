package com.licensis.notaire.testing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.application.port.out.document.DocumentTypeUsagePort;
import com.licensis.notaire.domain.document.DocumentType;

/** In-memory adapter for the document type ports: the use cases need no Spring or database. */
public class InMemoryDocumentTypeCatalog implements DocumentTypeCatalogPort, DocumentTypeUsagePort {

    private final Map<Integer, DocumentType> rows = new LinkedHashMap<>();
    private final Set<Integer> inUse = new HashSet<>();
    private int nextId = 1;

    public InMemoryDocumentTypeCatalog withInUse(int id) {
        inUse.add(id);
        return this;
    }

    @Override
    public List<DocumentType> findAll() {
        return new ArrayList<>(rows.values());
    }

    @Override
    public List<DocumentType> findByNameContaining(String text) {
        return rows.values().stream().filter(type -> type.name() != null && type.name().contains(text)).toList();
    }

    @Override
    public Optional<DocumentType> findById(int id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public DocumentType save(DocumentType type) {
        int id = type.id() != null ? type.id() : nextId++;
        int version = type.id() != null ? type.version() + 1 : 0;
        DocumentType saved = type.withIdentity(id, version);
        rows.put(id, saved);
        return saved;
    }

    @Override
    public boolean deleteById(int id) {
        return rows.remove(id) != null;
    }

    @Override
    public boolean isInUse(int id) {
        return inUse.contains(id);
    }
}
