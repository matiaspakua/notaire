package com.licensis.notaire.application.usecase.concept;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.application.port.out.concept.ConceptRepositoryPort;
import com.licensis.notaire.business.Concept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Concept domain operations.
 * Handles business logic for managing catalog concepts.
 */
@Service
@Transactional
public class ConceptService {

    private static final Logger log = LoggerFactory.getLogger(ConceptService.class);

    private final ConceptRepositoryPort conceptRepository;

    public ConceptService(ConceptRepositoryPort conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    /**
     * Returns all concepts.
     *
     * @return list of all concepts
     */
    @Transactional(readOnly = true)
    public List<Concept> findAll() {
        log.debug("Finding all concepts");
        return conceptRepository.findAll();
    }

    /**
     * Finds a concept by its primary key.
     *
     * @param id concept ID
     * @return optional containing concept, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Concept> findById(Integer id) {
        log.debug("Finding concept by id: {}", id);
        return conceptRepository.findById(id);
    }

    /**
     * Searches for concepts whose name contains the given string.
     *
     * @param name search term
     * @return list of matching concepts
     */
    @Transactional(readOnly = true)
    public List<Concept> searchByName(String name) {
        log.debug("Searching concepts by name: {}", name);
        return conceptRepository.findByNameContaining(name);
    }

    /**
     * Persists a new concept.
     *
     * @param concept entity to persist
     * @return saved concept with generated ID
     */
    public Concept create(Concept concept) {
        log.info("Creating concept: {}", concept.getName());
        concept.setEnabled(true);
        return conceptRepository.create(concept);
    }

    /**
     * Updates an existing concept.
     *
     * @param id concept ID
     * @param concept updated state
     * @return saved concept
     * @throws ResourceNotFoundException if no concept with given ID exists
     */
    public Concept update(Integer id, Concept concept) {
        if (!conceptRepository.existsById(id)) {
            throw new ResourceNotFoundException("Concept not found with ID: " + id);
        }
        log.info("Updating concept id: {}", id);
        return conceptRepository.update(id, concept);
    }

    /**
     * Deletes a concept by ID.
     *
     * @param id concept ID
     * @throws ResourceNotFoundException if no concept with given ID exists
     */
    public void deleteById(Integer id) {
        if (!conceptRepository.existsById(id)) {
            throw new ResourceNotFoundException("Concept not found with ID: " + id);
        }
        log.info("Deleting concept id: {}", id);
        conceptRepository.deleteById(id);
    }
}
