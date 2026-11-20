package com.licensis.notaire.adapter.out.persistence.management;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.domain.management.ManagementStatus;

/**
 * Remembers the management-status list and drops it when a row is written (#597, CU74). The application layer
 * cannot depend on Spring's cache annotations (hexagon rule R2), so the cache is an outbound adapter in front of
 * {@link ManagementStatusPersistenceAdapter}. Name search and get-by-id stay on the delegate.
 */
public class CachingManagementStatusCatalog implements ManagementStatusCatalogPort {

    public static final String CACHE = "management-statuses";

    private final ManagementStatusCatalogPort delegate;

    public CachingManagementStatusCatalog(ManagementStatusCatalogPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(CACHE)
    public List<ManagementStatus> findAll() {
        return delegate.findAll();
    }

    @Override
    public List<ManagementStatus> findByNameContaining(String text) {
        return delegate.findByNameContaining(text);
    }

    @Override
    public Optional<ManagementStatus> findById(int id) {
        return delegate.findById(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public ManagementStatus save(ManagementStatus status) {
        return delegate.save(status);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public boolean deleteById(int id) {
        return delegate.deleteById(id);
    }
}
