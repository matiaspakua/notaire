package com.licensis.notaire.application.port.out.testimony;

import com.licensis.notaire.business.TestimonyMovement;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for testimony-movement persistence operations.
 */
public interface TestimonyMovementRepositoryPort {

    List<TestimonyMovement> findAll();

    Optional<TestimonyMovement> findById(Integer id);

    TestimonyMovement save(TestimonyMovement entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
