package com.licensis.notaire.adapter.out.persistence.user;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.licensis.notaire.application.port.out.user.RoleCatalogPort;
import com.licensis.notaire.domain.user.Role;

/**
 * Remembers the role list and drops it when a row is written (#597, CU74). The application layer cannot depend
 * on Spring's cache annotations (hexagon rule R2), so the cache is an outbound adapter in front of
 * {@link RoleCatalogPersistenceAdapter}. Lookup by id or name stays on the delegate.
 */
public class CachingRoleCatalog implements RoleCatalogPort {

    public static final String CACHE = "roles";

    private final RoleCatalogPort delegate;

    public CachingRoleCatalog(RoleCatalogPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(CACHE)
    public List<Role> findAll() {
        return delegate.findAll();
    }

    @Override
    public Optional<Role> findById(int id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return delegate.findByName(name);
    }

    @Override
    public boolean existsById(int id) {
        return delegate.existsById(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public Role save(Role role) {
        return delegate.save(role);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public boolean deleteById(int id) {
        return delegate.deleteById(id);
    }
}
