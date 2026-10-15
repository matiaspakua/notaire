package com.licensis.notaire.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.licensis.notaire.application.port.out.document.SubmittedDocumentCatalogPort;
import com.licensis.notaire.domain.document.SubmittedDocument;

/** In-memory catalog for submitted-document use-case tests (issue #1726). */
public class InMemorySubmittedDocumentCatalog implements SubmittedDocumentCatalogPort {

    private final AtomicInteger seq = new AtomicInteger(1);
    private final List<SubmittedDocument> rows = new ArrayList<>();

    @Override
    public List<SubmittedDocument> findAll() {
        return List.copyOf(rows);
    }

    @Override
    public Optional<SubmittedDocument> findById(int id) {
        return rows.stream().filter(d -> d.id() != null && d.id() == id).findFirst();
    }

    @Override
    public SubmittedDocument save(SubmittedDocument document) {
        if (document.id() == null) {
            SubmittedDocument stored = document.withIdentity(seq.getAndIncrement());
            rows.add(stored);
            return stored;
        }
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).id().equals(document.id())) {
                rows.set(i, document);
                return document;
            }
        }
        rows.add(document);
        return document;
    }

    @Override
    public boolean existsById(int id) {
        return findById(id).isPresent();
    }

    @Override
    public boolean deleteById(int id) {
        return rows.removeIf(d -> d.id() != null && d.id() == id);
    }
}
