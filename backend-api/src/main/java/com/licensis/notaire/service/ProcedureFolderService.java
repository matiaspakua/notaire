package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CU85 - Administrar Carpetas de Trámite: genera automáticamente una carpeta
 * por cada trámite dado de alta y administra su ciclo de vida
 * activa/espera/archivada.
 */
@Service
@Transactional
public class ProcedureFolderService {

    static final String StatusACTIVA = "Activa";
    static final String StatusWait = "Espera";

    private static final Logger log = LoggerFactory.getLogger(ProcedureFolderService.class);

    private final ProcedureFolderRepository procedureFolderRepository;

    public ProcedureFolderService(ProcedureFolderRepository procedureFolderRepository) {
        this.procedureFolderRepository = procedureFolderRepository;
    }

    public ProcedureFolder generateFolderForProcedure(Procedure procedure) {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setNumber(calculateNextNumber());
        folder.setStatus(StatusACTIVA);
        folder.setFkIdManagement(procedure.getFkIdManagement());
        folder.setFkIdProcedure(procedure);
        ProcedureFolder guardada = procedureFolderRepository.save(folder);
        log.info("Carpeta {} generada para trámite {} de gestión {}", guardada.getNumber(),
                procedure.getIdProcedure(), procedure.getFkIdManagement().getIdManagement());
        return guardada;
    }

    private int calculateNextNumber() {
        return procedureFolderRepository.findTopByOrderByNumberDesc()
                .map(c -> c.getNumber() + 1)
                .orElse(1);
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
                    "El motivo es obligatorio para poner una carpeta en espera");
        }
        ProcedureFolder folder = procedureFolderRepository.findById(idFolder)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la carpeta con ID: " + idFolder));
        folder.setStatus(StatusWait);
        folder.setWaitReason(reason);
        return procedureFolderRepository.save(folder);
    }
}
