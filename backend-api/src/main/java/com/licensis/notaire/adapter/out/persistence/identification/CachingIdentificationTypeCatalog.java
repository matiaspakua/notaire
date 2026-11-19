package com.licensis.notaire.adapter.out.persistence.identification;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;
import com.licensis.notaire.domain.identification.IdentificationType;

/**
 * Remembers the identification-type list and drops it when a row is written (#597, CU74). The application
 * layer cannot depend on Spring's cache annotations (hexagon rule R2), so the cache is an outbound adapter
 * in front of {@link IdentificationTypePersistenceAdapter}.
 */
public class CachingIdentificationTypeCatalog implements IdentificationTypeCatalogPort {

    public static final String CACHE = "identification-types";

    private final IdentificationTypeCatalogPort delegate;

    public CachingIdentificationTypeCatalog(IdentificationTypeCatalogPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(CACHE)
    public List<IdentificationType> findAll() {
        return delegate.findAll();
    }

    @Override
    public Optional<IdentificationType> findById(int id) {
        return delegate.findById(id);
    }

    @Override
    public boolean existsById(int id) {
        return delegate.existsById(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public IdentificationType save(IdentificationType type) {
        return delegate.save(type);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public void deleteById(int id) {
        delegate.deleteById(id);
    }
}
