package com.licensis.notaire.adapter.out.persistence.concept;

import com.licensis.notaire.application.port.out.concept.ConceptRepositoryPort;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.repository.ConceptRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link ConceptRepositoryPort} on top the existing
 * Spring Data JPA ConceptRepository.
 *
 * <p>This is the only place where JPA Concept entity is accessed within the
 * concept slice: every value returned is a detached Concept instance.
 */
@Component
public class ConceptPersistenceAdapter implements ConceptRepositoryPort {

    private final ConceptRepository conceptRepository;

    public ConceptPersistenceAdapter(ConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    @Override
    public List<Concept> findAll() {
        return conceptRepository.findAll();
    }

    @Override
    public Optional<Concept> findById(Integer id) {
        return conceptRepository.findById(id);
    }

    @Override
    public List<Concept> findByNameContaining(String name) {
        return conceptRepository.findByNameContaining(name);
    }

    @Override
    public boolean existsById(Integer id) {
        return conceptRepository.existsById(id);
    }

    @Override
    public Concept create(Concept concept) {
        return conceptRepository.save(concept);
    }

    @Override
    public Concept update(Integer id, Concept concept) {
        concept.setIdConcept(id);
        return conceptRepository.save(concept);
    }

    @Override
    public void deleteById(Integer id) {
        conceptRepository.deleteById(id);
    }
}
