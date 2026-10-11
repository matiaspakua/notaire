package com.licensis.notaire.application.usecase.management;

import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.exception.FoldersInWaitException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.domain.payment.Money;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CU16 — Archive a management: computes the management's aggregated outstanding
 * balance (RF-22) and records whether it was archived with debt (RF-37).
 * Status-transition checks, including the history entry, are delegated to
 * {@link ManagementTransitionService} (CU83, CU13).
 */
@Service
public class ManagementArchiveDebtService {

    private static final String StatusARCHIVADA = "Archivada";
    private static final String StatusWait = "Espera";

    private static final Logger log = LoggerFactory.getLogger(ManagementArchiveDebtService.class);

    private final DeedManagementRepository managementRepository;
    private final ProcedureRepository procedureRepository;
    private final GetPaymentStatusUseCase paymentStatus;
    private final ManagementTransitionService managementTransitionService;
    private final ProcedureFolderRepository procedureFolderRepository;

    public ManagementArchiveDebtService(DeedManagementRepository managementRepository,
            ProcedureRepository procedureRepository, GetPaymentStatusUseCase paymentStatus,
            ManagementTransitionService managementTransitionService,
            ProcedureFolderRepository procedureFolderRepository) {
        this.managementRepository = managementRepository;
        this.procedureRepository = procedureRepository;
        this.paymentStatus = paymentStatus;
        this.managementTransitionService = managementTransitionService;
        this.procedureFolderRepository = procedureFolderRepository;
    }

    /**
     * Sums the outstanding balance of each budget linked to the management's procedures.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePendingBalance(Integer idManagement) {
        managementRepository.findById(idManagement)
                .orElseThrow(() -> new IllegalArgumentException("Management not found with ID: " + idManagement));

        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(idManagement);
        Set<Integer> countedBudgetIds = new HashSet<>();
        BigDecimal pendingBalance = Money.zero();
        for (Procedure procedure : procedures) {
            Budget budget = procedure.getFkIdBudget();
            if (budget == null || !countedBudgetIds.add(budget.getIdBudget())) {
                continue;
            }
            pendingBalance = Money.of(pendingBalance.add(paymentStatus.pendingBalance(budget.getIdBudget())));
        }

        log.debug("Aggregated outstanding balance for management {}: {}", idManagement, pendingBalance);
        return pendingBalance;
    }

    /**
     * Result of archiving a management: the archived management and the aggregated
     * outstanding balance calculated at that moment.
     */
    public record ArchiveResult(DeedManagement management, BigDecimal pendingBalance) { }

    /**
     * CU16 — Archives the management without requiring confirmation of folders in wait.
     * Equivalent to {@code archiving(idManagement, false)}.
     */
    public ArchiveResult archiving(Integer idManagement) {
        return archiving(idManagement, false);
    }

    /**
     * CU16 — Archives the management, moves every procedure folder to the stored
     * status "Archivada" (CU85), and records whether debt was still outstanding
     * (RF-22, RF-37). Debt does not block the archive: the outstanding-balance
     * warning is shown before confirmation (GET /outstanding-balance), and the
     * archive is stored with or without debt. If a folder is still in the stored
     * status "Espera" and {@code confirmado} is false, the archive is rejected
     * with {@link FoldersInWaitException} (CU85 — exception 5.1).
     */
    @Transactional
    public ArchiveResult archiving(Integer idManagement, boolean confirmado) {
        List<ProcedureFolder> foldersInWait =
                procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(idManagement, StatusWait);
        if (!foldersInWait.isEmpty() && !confirmado) {
            throw new FoldersInWaitException(
                    "The management has procedure folders on hold that are unresolved; "
                            + "confirm explicitly to archive anyway",
                    foldersInWait);
        }

        BigDecimal pendingBalance = calculatePendingBalance(idManagement);

        DeedManagement management = managementTransitionService.transition(idManagement, StatusARCHIVADA);
        management.setPendingDebtAtArchiving(
                pendingBalance != null && pendingBalance.compareTo(BigDecimal.ZERO) > 0);
        DeedManagement archivedManagement = managementRepository.save(management);

        archiveFolders(idManagement);

        log.info("Management {} archivada with deudaPendienteAlArchivar={}", idManagement,
                archivedManagement.getPendingDebtAtArchiving());
        return new ArchiveResult(archivedManagement, pendingBalance);
    }

    private void archiveFolders(Integer idManagement) {
        List<ProcedureFolder> carpetas = procedureFolderRepository.findByFkIdManagementIdManagement(idManagement);
        carpetas.forEach(folder -> folder.setStatus(StatusARCHIVADA));
        procedureFolderRepository.saveAll(carpetas);
    }
}
