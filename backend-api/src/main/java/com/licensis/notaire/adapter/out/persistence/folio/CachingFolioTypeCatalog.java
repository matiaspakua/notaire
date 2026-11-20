package com.licensis.notaire.adapter.out.persistence.folio;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.domain.folio.FolioType;

/**
 * Remembers the folio-type list and drops it when a row is written (#597, CU74). The application layer cannot
 * depend on Spring's cache annotations (hexagon rule R2), so the cache is an outbound adapter in front of
 * {@link FolioTypePersistenceAdapter}. Name search and get-by-id stay on the delegate.
 */
public class CachingFolioTypeCatalog implements FolioTypeCatalogPort {

    public static final String CACHE = "folio-types";

    private final FolioTypeCatalogPort delegate;

    public CachingFolioTypeCatalog(FolioTypeCatalogPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(CACHE)
    public List<FolioType> findAll() {
        return delegate.findAll();
    }

    @Override
    public List<FolioType> findByNameContaining(String text) {
        return delegate.findByNameContaining(text);
    }

    @Override
    public Optional<FolioType> findById(int id) {
        return delegate.findById(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public FolioType save(FolioType type) {
        return delegate.save(type);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public boolean deleteById(int id) {
        return delegate.deleteById(id);
    }
}
