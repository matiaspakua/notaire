package com.licensis.notaire.application.port.in.procedure;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoProcedureType;

/**
 * Read-side inbound port for procedure types (issue #1791).
 */
public interface QueryProcedureTypesUseCase {

    List<DtoProcedureType> findAll();

    List<DtoProcedureType> searchByName(String name);

    Optional<DtoProcedureType> findById(Integer id);

    Optional<Boolean> isInUse(Integer id);
}
