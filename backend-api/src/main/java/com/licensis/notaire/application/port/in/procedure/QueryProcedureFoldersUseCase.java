package com.licensis.notaire.application.port.in.procedure;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.dto.DtoProcedureFolder;

/**
 * Inbound query port for procedure folders (issue #1795).
 */
public interface QueryProcedureFoldersUseCase {

    Optional<DtoProcedureFolder> findById(Integer id);

    List<DtoProcedureFolder> search(Integer managementId, Integer procedureId);
}
