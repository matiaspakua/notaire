package com.licensis.notaire.service;

import com.licensis.notaire.exception.CarpetasEnWaitException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CU16 - Archivar Gestión: calcula el saldo pendiente agregado de una gestión
 * (RF-22) y persiste si quedó archivada con deuda (RF-37). Delega la
 * validación de la transición de estado (y su registro en bitácora) a
 * {@link GestionTransitionService} (CU83, CU13).
 */
@Service
public class ManagementArchiveDebtService {

    private static final String StatusARCHIVADA = "Archivada";
    private static final String StatusWait = "Espera";

    private static final Logger log = LoggerFactory.getLogger(ManagementArchiveDebtService.class);

    private final DeedManagementRepository managementRepository;
    private final ProcedureRepository procedureRepository;
    private final PaymentService paymentService;
    private final ManagementTransitionService managementTransitionService;
    private final ProcedureFolderRepository procedureFolderRepository;

    public ManagementArchiveDebtService(DeedManagementRepository managementRepository,
            ProcedureRepository procedureRepository, PaymentService paymentService,
            ManagementTransitionService managementTransitionService,
            ProcedureFolderRepository procedureFolderRepository) {
        this.managementRepository = managementRepository;
        this.procedureRepository = procedureRepository;
        this.paymentService = paymentService;
        this.managementTransitionService = managementTransitionService;
        this.procedureFolderRepository = procedureFolderRepository;
    }

    /**
     * Suma el saldo pendiente de cada presupuesto vinculado a los trámites de la gestión.
     */
    @Transactional(readOnly = true)
    public Float calcularSaldoPending(Integer idManagement) {
        managementRepository.findById(idManagement)
                .orElseThrow(() -> new IllegalArgumentException("Gestión no encontrada con ID: " + idManagement));

        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(idManagement);
        Set<Integer> idsBudgetContados = new HashSet<>();
        float saldo = 0f;
        for (Procedure procedure : procedures) {
            Budget budget = procedure.getFkIdBudget();
            if (budget == null || !idsBudgetContados.add(budget.getIdBudget())) {
                continue;
            }
            Float saldoBudget = paymentService.calcularSaldoPending(budget.getIdBudget());
            saldo += saldoBudget != null ? saldoBudget : 0f;
        }

        log.debug("Saldo pendiente agregado para gestión {}: {}", idManagement, saldo);
        return saldo;
    }

    /**
     * Resultado de archivar una gestión: la gestión ya archivada junto con el
     * saldo pendiente agregado calculado en el momento del archivado.
     */
    public record ArchiveResult(DeedManagement management, Float saldoPending) { }

    /**
     * CU16 - Archiva la gestión sin exigir confirmación de carpetas en espera.
     * Equivalente a {@code archivar(idGestion, false)}.
     */
    public ArchiveResult archiving(Integer idManagement) {
        return archiving(idManagement, false);
    }

    /**
     * CU16 - Archiva la gestión, cambia todas sus carpetas de trámite a
     * "Archivada" (CU85) y registra si quedó con deuda pendiente (RF-22,
     * RF-37). El archivado no se bloquea por la existencia de deuda: la
     * advertencia de saldo pendiente se muestra al usuario antes de confirmar
     * (ver GET /saldo-pendiente), pero la confirmación del archivado siempre
     * se persiste con o sin deuda. Si alguna carpeta sigue en "Espera" y
     * {@code confirmado} es falso, se rechaza el archivado con
     * {@link CarpetasEnEsperaException} (CU85 — Excepción 5.1).
     */
    @Transactional
    public ArchiveResult archiving(Integer idManagement, boolean confirmado) {
        List<ProcedureFolder> carpetasEnWait =
                procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(idManagement, StatusWait);
        if (!carpetasEnWait.isEmpty() && !confirmado) {
            throw new CarpetasEnWaitException(
                    "La gestión tiene carpetas de trámite en espera sin resolver; "
                            + "confirme explícitamente para archivar de todos modos",
                    carpetasEnWait);
        }

        Float saldoPending = calcularSaldoPending(idManagement);

        DeedManagement management = managementTransitionService.transicionar(idManagement, StatusARCHIVADA);
        management.setPendingDebtAtArchiving(saldoPending != null && saldoPending > 0);
        DeedManagement archivedManagement = managementRepository.save(management);

        archivingCarpetas(idManagement);

        log.info("Gestión {} archivada con deudaPendienteAlArchivar={}", idManagement,
                archivedManagement.getPendingDebtAtArchiving());
        return new ArchiveResult(archivedManagement, saldoPending);
    }

    private void archivingCarpetas(Integer idManagement) {
        List<ProcedureFolder> carpetas = procedureFolderRepository.findByFkIdManagementIdManagement(idManagement);
        carpetas.forEach(folder -> folder.setStatus(StatusARCHIVADA));
        procedureFolderRepository.saveAll(carpetas);
    }
}
