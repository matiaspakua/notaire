package com.licensis.notaire.application.port.out.testimony;

import com.licensis.notaire.business.Testimony;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for testimony persistence operations.
 */
public interface TestimonyRepositoryPort {

    List<Testimony> findAll();

    Optional<Testimony> findById(Integer id);

    Testimony save(Testimony entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
