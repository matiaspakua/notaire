package com.licensis.notaire.adapter.out.persistence.concept;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.licensis.notaire.application.port.out.concept.ConceptOpsPort;
import com.licensis.notaire.dto.DtoConcept;

/**
 * Remembers the concept list and drops it when a row is written (#597, CU74). The application layer cannot depend
 * on Spring's cache annotations (hexagon rule R2), so the cache is an outbound adapter in front of
 * {@link ConceptOpsAdapter}. Name search and get-by-id stay on the delegate.
 */
public class CachingConceptCatalog implements ConceptOpsPort {

    public static final String CACHE = "concepts";

    private final ConceptOpsPort delegate;

    public CachingConceptCatalog(ConceptOpsPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(CACHE)
    public List<DtoConcept> findAll() {
        return delegate.findAll();
    }

    @Override
    public List<DtoConcept> searchByName(String name) {
        return delegate.searchByName(name);
    }

    @Override
    public Optional<DtoConcept> findById(Integer id) {
        return delegate.findById(id);
    }

    @Override
    public InUseResult isInUse(Integer id) {
        return delegate.isInUse(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public SaveResult create(DtoConcept dto) {
        return delegate.create(dto);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public SaveResult update(Integer id, DtoConcept dto) {
        return delegate.update(id, dto);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public DeleteResult delete(Integer id) {
        return delegate.delete(id);
    }
}
