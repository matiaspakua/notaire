package com.licensis.notaire.application.port.in.testimony;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoTestimony;

/**
 * Read-side inbound port for testimonies (issue #1783).
 */
public interface QueryTestimoniesUseCase {

    List<DtoTestimony> findAll();

    Optional<DtoTestimony> findById(Integer id);
}
