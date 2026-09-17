package com.licensis.notaire.application.port.in.testimony;

import com.licensis.notaire.business.TestimonyMovement;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port for testimony-movement operations.
 */
public interface TestimonyMovementUseCase {

    List<TestimonyMovement> findAll();

    Optional<TestimonyMovement> findById(Integer id);

    TestimonyMovement save(TestimonyMovement entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
