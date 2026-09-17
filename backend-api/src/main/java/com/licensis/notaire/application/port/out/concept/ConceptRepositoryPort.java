package com.licensis.notaire.application.port.out.concept;

import com.licensis.notaire.business.Concept;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port describing concept persistence capabilities application needs.
 *
 * <p>Exposes only operations use cases actually exercise, enabling
 * manipulation Concept entities independently JPA repository details.
 */
public interface ConceptRepositoryPort {

    List<Concept> findAll();

    Optional<Concept> findById(Integer id);

    List<Concept> findByNameContaining(String name);

    boolean existsById(Integer id);

    Concept create(Concept concept);

    Concept update(Integer id, Concept concept);

    void deleteById(Integer id);
}
