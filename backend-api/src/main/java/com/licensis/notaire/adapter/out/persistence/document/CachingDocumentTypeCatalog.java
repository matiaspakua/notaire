package com.licensis.notaire.adapter.out.persistence.document;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.domain.document.DocumentType;

/**
 * Remembers the document-type list and drops it when a row is written (#597, CU74). The application layer cannot
 * depend on Spring's cache annotations (hexagon rule R2), so the cache is an outbound adapter in front of
 * {@link DocumentTypePersistenceAdapter}. Name search and get-by-id stay on the delegate.
 */
public class CachingDocumentTypeCatalog implements DocumentTypeCatalogPort {

    public static final String CACHE = "document-types";

    private final DocumentTypeCatalogPort delegate;

    public CachingDocumentTypeCatalog(DocumentTypeCatalogPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(CACHE)
    public List<DocumentType> findAll() {
        return delegate.findAll();
    }

    @Override
    public List<DocumentType> findByNameContaining(String text) {
        return delegate.findByNameContaining(text);
    }

    @Override
    public Optional<DocumentType> findById(int id) {
        return delegate.findById(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public DocumentType save(DocumentType type) {
        return delegate.save(type);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public boolean deleteById(int id) {
        return delegate.deleteById(id);
    }
}
