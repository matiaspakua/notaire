package com.licensis.notaire.application.port.out.procedure;

import com.licensis.notaire.business.ProcedureFolder;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for procedure folder persistence operations.
 */
public interface ProcedureFolderRepositoryPort {

    Optional<ProcedureFolder> findById(Integer id);

    ProcedureFolder save(ProcedureFolder entity);

    void deleteById(Integer id);

    Optional<ProcedureFolder> findByFkIdProcedureIdProcedure(Integer idProcedure);

    List<ProcedureFolder> findByFkIdManagementIdManagement(Integer idManagement);

    int nextFolderNumber();
}
