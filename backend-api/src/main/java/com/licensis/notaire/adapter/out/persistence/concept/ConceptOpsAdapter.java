package com.licensis.notaire.adapter.out.persistence.concept;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.concept.ConceptOpsPort;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.dto.DtoConcept;
import com.licensis.notaire.repository.BudgetTemplateRepository;

/**
 * Persistence/ops adapter for concepts. Holds entity/BudgetTemplateRepository access previously on
 * {@code ConceptController} (issue #1815).
 */
@Component
public class ConceptOpsAdapter implements ConceptOpsPort {

    private final ConceptService service;
    private final BudgetTemplateRepository templateRepository;

    public ConceptOpsAdapter(ConceptService service, BudgetTemplateRepository templateRepository) {
        this.service = service;
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoConcept> findAll() {
        return service.findAll().stream().map(ConceptLegacyMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoConcept> searchByName(String name) {
        return service.searchByName(name).stream().map(ConceptLegacyMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoConcept> findById(Integer id) {
        return service.findById(id).map(ConceptLegacyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public InUseResult isInUse(Integer id) {
        if (service.findById(id).isEmpty()) {
            return new InUseResult.NotFound();
        }
        return new InUseResult.Ok(!templateRepository.findByConceptIdConcept(id).isEmpty());
    }

    @Override
    public SaveResult create(DtoConcept dto) {
        // No outer @Transactional: catching failures into SaveResult.Failed must not mark the
        // Spring TX rollback-only (UnexpectedRollbackException → 500 instead of 400).
        try {
            if (dto.getEnabled() == null) {
                dto.setEnabled(true);
            }
            Concept entity = new Concept();
            ConceptLegacyMapper.apply(entity, dto);
            return new SaveResult.Created(ConceptLegacyMapper.toDto(service.create(entity)));
        } catch (Exception e) {
            return new SaveResult.Failed(e, true);
        }
    }

    @Override
    public SaveResult update(Integer id, DtoConcept dto) {
        try {
            var existing = service.findById(id);
            if (existing.isEmpty()) {
                return new SaveResult.NotFound();
            }
            if (!templateRepository.findByConceptIdConcept(id).isEmpty()) {
                return new SaveResult.InUse("This concept is in use and cannot be modified. Create a new concept.");
            }
            dto.setIdConcept(id);
            Concept entity = existing.get();
            if (dto.getEnabled() == null) {
                dto.setEnabled(entity.getEnabled());
            }
            ConceptLegacyMapper.apply(entity, dto);
            service.update(id, entity);
            return new SaveResult.Updated();
        } catch (Exception e) {
            return new SaveResult.Failed(e, false);
        }
    }

    @Override
    public DeleteResult delete(Integer id) {
        try {
            if (service.findById(id).isEmpty()) {
                return new DeleteResult.NotFound();
            }
            if (!templateRepository.findByConceptIdConcept(id).isEmpty()) {
                return new DeleteResult.InUse("Cannot delete: the concept is in use on budget templates.");
            }
            service.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            return new DeleteResult.Conflict(e);
        }
    }
}
