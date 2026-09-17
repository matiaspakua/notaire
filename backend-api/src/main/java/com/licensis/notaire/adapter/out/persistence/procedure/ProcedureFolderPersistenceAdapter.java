package com.licensis.notaire.adapter.out.persistence.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureFolderRepositoryPort;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the procedure folder slice.
 */
@Component
public class ProcedureFolderPersistenceAdapter implements ProcedureFolderRepositoryPort {

    private final ProcedureFolderRepository procedureFolderRepository;

    public ProcedureFolderPersistenceAdapter(ProcedureFolderRepository procedureFolderRepository) {
        this.procedureFolderRepository = procedureFolderRepository;
    }

    @Override
    public Optional<ProcedureFolder> findById(Integer id) {
        return procedureFolderRepository.findById(id);
    }

    @Override
    public ProcedureFolder save(ProcedureFolder entity) {
        return procedureFolderRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        procedureFolderRepository.deleteById(id);
    }

    @Override
    public Optional<ProcedureFolder> findByFkIdProcedureIdProcedure(Integer idProcedure) {
        return procedureFolderRepository.findByFkIdProcedureIdProcedure(idProcedure);
    }

    @Override
    public List<ProcedureFolder> findByFkIdManagementIdManagement(Integer idManagement) {
        return procedureFolderRepository.findByFkIdManagementIdManagement(idManagement);
    }

    @Override
    public Optional<ProcedureFolder> findTopByOrderByNumberDesc() {
        return procedureFolderRepository.findTopByOrderByNumberDesc();
    }
}
