package com.licensis.notaire.application.usecase.procedure;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.application.port.out.procedure.ProcedureFolderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CU85 - Administrar Procedure folders: generates automáticamente a folder
 * by each procedure dado of alta and administra its ciclo of vida
 * activa/wait/archivada.
 */
@Service
@Transactional
public class ProcedureFolderService {

    static final String StatusACTIVA = "Activa";
    static final String StatusWait = "Espera";

    private static final Logger log = LoggerFactory.getLogger(ProcedureFolderService.class);

    private final ProcedureFolderRepositoryPort procedureFolderRepository;

    public ProcedureFolderService(ProcedureFolderRepositoryPort procedureFolderRepository) {
        this.procedureFolderRepository = procedureFolderRepository;
    }

    public ProcedureFolder generateFolderForProcedure(Procedure procedure) {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setNumber(procedureFolderRepository.nextFolderNumber());
        folder.setStatus(StatusACTIVA);
        folder.setFkIdManagement(procedure.getFkIdManagement());
        folder.setFkIdProcedure(procedure);
        ProcedureFolder guardada = procedureFolderRepository.save(folder);
        log.info("Folder {} generada for procedure {} of management {}", guardada.getNumber(),
                procedure.getIdProcedure(), procedure.getFkIdManagement().getIdManagement());
        return guardada;
    }

    @Transactional(readOnly = true)
    public Optional<ProcedureFolder> findById(Integer id) {
        return procedureFolderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ProcedureFolder> findByProcedure(Integer idProcedure) {
        return procedureFolderRepository.findByFkIdProcedureIdProcedure(idProcedure);
    }

    @Transactional(readOnly = true)
    public List<ProcedureFolder> findByManagement(Integer idManagement) {
        return procedureFolderRepository.findByFkIdManagementIdManagement(idManagement);
    }

    public ProcedureFolder ponerEnWait(Integer idFolder, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessValidationException(
                    "The reason is required to put a folder on hold");
        }
        ProcedureFolder folder = procedureFolderRepository.findById(idFolder)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with ID: " + idFolder));
        folder.setStatus(StatusWait);
        folder.setWaitReason(reason);
        return procedureFolderRepository.save(folder);
    }
}
