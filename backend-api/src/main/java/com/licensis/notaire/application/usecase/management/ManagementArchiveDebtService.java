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
 * CU16 - Archivar Management: calcula the outstanding balance agregado of a management
 * (RF-22) and persiste if quedó archivada with debt (RF-37). Delega the
 * validación of the transition of status (and its registro in bitácora) a
 * {@link GestionTransitionService} (CU83, CU13).
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
     * Suma the outstanding balance of each budget vinculado a the procedures of the management.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePendingBalance(Integer idManagement) {
        managementRepository.findById(idManagement)
                .orElseThrow(() -> new IllegalArgumentException("Management not found with ID: " + idManagement));

        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(idManagement);
        Set<Integer> idsBudgetContados = new HashSet<>();
        BigDecimal saldo = Money.zero();
        for (Procedure procedure : procedures) {
            Budget budget = procedure.getFkIdBudget();
            if (budget == null || !idsBudgetContados.add(budget.getIdBudget())) {
                continue;
            }
            saldo = Money.of(saldo.add(paymentStatus.pendingBalance(budget.getIdBudget())));
        }

        log.debug("Outstanding balance agregado for management {}: {}", idManagement, saldo);
        return saldo;
    }

    /**
     * Resultado of archivar a management: the management already archivada together with the
     * outstanding balance agregado calculado in the momento of the archivado.
     */
    public record ArchiveResult(DeedManagement management, BigDecimal pendingBalance) { }

    /**
     * CU16 - Archiva the management without exigir confirmación of folders in espera.
     * Equivalente a {@code archivar(idGestion, false)}.
     */
    public ArchiveResult archiving(Integer idManagement) {
        return archiving(idManagement, false);
    }

    /**
     * CU16 - Archiva the management, cambia all their procedure folders a
     * "Archivada" (CU85) and records if quedó with debt pendiente (RF-22,
     * RF-37). The archivado not bloquea by the existencia of debt: the
     * advertencia of outstanding balance muestra to the user antes of confirmar
     * (ver GET /outstanding-balance), pero the confirmación of the archivado siempre
     *  persiste with or without debt. If alguna folder sigue in "Espera" and
     * {@code confirmado} es falso, se rechaza el archivado con
     * {@link CarpetasEnEsperaException} (CU85 — Excepción 5.1).
     */
    @Transactional
    public ArchiveResult archiving(Integer idManagement, boolean confirmado) {
        List<ProcedureFolder> foldersInWait =
                procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(idManagement, StatusWait);
        if (!foldersInWait.isEmpty() && !confirmado) {
            throw new FoldersInWaitException(
                    "The management has procedure folders in espera without resolver; "
                            + "confirme explícitamente for archivar of all modos",
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
