package com.licensis.notaire.application.usecase.management;

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
 * CU13 - Writes and reads the status History (bitácora) for a management.
 */
@Service
public class ManagementBitacoraService {

    private final HistoryRepository historyRepository;

    public ManagementBitacoraService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * Appends the management's current status to History (create, valid
     * transition, archive, or other status write paths).
     */
    @Transactional
    public History registerStatus(DeedManagement management, String notes) {
        ManagementStatus currentStatus = management.getFkIdManagementStatus();
        if (currentStatus == null) {
            throw new BusinessValidationException(
                    "Cannot register History: management " + management.getIdManagement()
                            + " has no status assigned");
        }

        History history = new History();
        history.setFkIdManagement(management);
        history.setFkIdManagementStatus(currentStatus);
        history.setDate(new Date());
        history.setNotes(notes);

        return historyRepository.save(history);
    }

    /**
     * Returns the full History for the management, ordered chronologically.
     */
    @Transactional(readOnly = true)
    public List<History> getHistory(Integer idManagement) {
        List<History> history = historyRepository.findByFkIdManagementIdManagement(idManagement);
        return history.stream()
                .sorted(Comparator.comparing(History::getDate))
                .toList();
    }
}
