package com.licensis.notaire.application.port.in.testimony;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.PageResult;
import com.licensis.notaire.dto.DtoTestimonyMovement;

/**
 * Read-side inbound port for testimony movements (issue #1787).
 */
public interface QueryTestimonyMovementsUseCase {

    List<DtoTestimonyMovement> findAll();

    PageResult findPage(int page, int size, String sort);

    Optional<DtoTestimonyMovement> findById(Integer id);
}
