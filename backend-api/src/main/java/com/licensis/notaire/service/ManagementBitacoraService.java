package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.repository.HistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * CU13 - Escribe y consulta la bitácora ({@link Historial}) de cambios de
 * estado de una gestión.
 */
@Service
public class ManagementBitacoraService {

    private final HistoryRepository historyRepository;

    public ManagementBitacoraService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * Registra en la bitácora el estado actual de la gestión (alta, transición
     * válida o archivado).
     */
    @Transactional
    public History registrarStatus(DeedManagement management, String notes) {
        ManagementStatus statusActual = management.getFkIdManagementStatus();
        if (statusActual == null) {
            throw new BusinessValidationException(
                    "No se puede registrar bitácora: la gestión " + management.getIdManagement()
                            + " no tiene un estado asignado");
        }

        History history = new History();
        history.setFkIdManagement(management);
        history.setFkIdManagementStatus(statusActual);
        history.setDate(new Date());
        history.setNotes(notes);

        return historyRepository.save(history);
    }

    /**
     * Devuelve la bitácora completa de la gestión ordenada cronológicamente.
     */
    @Transactional(readOnly = true)
    public List<History> obtenerHistory(Integer idManagement) {
        List<History> history = historyRepository.findByFkIdManagementIdManagement(idManagement);
        return history.stream()
                .sorted(Comparator.comparing(History::getDate))
                .toList();
    }
}
