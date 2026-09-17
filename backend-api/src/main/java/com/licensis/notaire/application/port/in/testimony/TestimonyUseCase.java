package com.licensis.notaire.application.port.in.testimony;

import com.licensis.notaire.business.Testimony;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port for testimony operations.
 */
public interface TestimonyUseCase {

    List<Testimony> findAll();

    Optional<Testimony> findById(Integer id);

    Testimony save(Testimony entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
