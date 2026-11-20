package com.licensis.notaire.adapter.out.persistence.procedure;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort;
import com.licensis.notaire.dto.DtoProcedureType;

/**
 * Remembers the procedure-type list and drops it when a row is written (#597, CU74). The application layer cannot
 * depend on Spring's cache annotations (hexagon rule R2), so the cache is an outbound adapter in front of
 * {@link ProcedureTypeOpsAdapter}. Name search and get-by-id stay on the delegate. Assigning a workflow changes
 * the row the list shows, so it drops the list too.
 */
public class CachingProcedureTypeCatalog implements ProcedureTypeOpsPort {

    public static final String CACHE = "procedure-types";

    private final ProcedureTypeOpsPort delegate;

    public CachingProcedureTypeCatalog(ProcedureTypeOpsPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(CACHE)
    public List<DtoProcedureType> findAll() {
        return delegate.findAll();
    }

    @Override
    public List<DtoProcedureType> searchByName(String name) {
        return delegate.searchByName(name);
    }

    @Override
    public Optional<DtoProcedureType> findById(Integer id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<Boolean> isInUse(Integer id) {
        return delegate.isInUse(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public SaveResult create(DtoProcedureType dto) {
        return delegate.create(dto);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public SaveResult update(Integer id, DtoProcedureType dto) {
        return delegate.update(id, dto);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public DeleteResult delete(Integer id) {
        return delegate.delete(id);
    }

    @Override
    @CacheEvict(value = CACHE, allEntries = true)
    public WorkflowAssignResult assignWorkflow(Integer id, Integer workflowDefinitionId) {
        return delegate.assignWorkflow(id, workflowDefinitionId);
    }
}
