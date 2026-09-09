package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoManagementResumenFinanciero;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CU47/CU02 - Resumen financiero agregado de una gestión: total presupuestado,
 * total cobrado y saldo pendiente, sumados a través de los presupuestos de
 * cada trámite vinculado.
 */
@Service
public class ManagementResumenFinancieroService {

    private final ProcedureRepository procedureRepository;
    private final PaymentService paymentService;
    private final ManagementArchiveDebtService managementArchiveDebtService;

    public ManagementResumenFinancieroService(ProcedureRepository procedureRepository, PaymentService paymentService,
            ManagementArchiveDebtService managementArchiveDebtService) {
        this.procedureRepository = procedureRepository;
        this.paymentService = paymentService;
        this.managementArchiveDebtService = managementArchiveDebtService;
    }

    @Transactional(readOnly = true)
    public DtoManagementResumenFinanciero obtenerResumen(Integer idManagement) {
        Float saldoPending = managementArchiveDebtService.calcularSaldoPending(idManagement);

        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(idManagement);
        Set<Integer> idsBudgetContados = new HashSet<>();
        float totalPresupuestado = 0f;
        float totalCobrado = 0f;

        for (Procedure procedure : procedures) {
            Budget budget = procedure.getFkIdBudget();
            if (budget == null || !idsBudgetContados.add(budget.getIdBudget())) {
                continue;
            }
            Float saldoBudget = paymentService.calcularSaldoPending(budget.getIdBudget());
            float cobradoBudget = (float) paymentService.findPaymentsByBudget(budget.getIdBudget())
                    .stream().mapToDouble(p -> p.getAmount()).sum();
            totalCobrado += cobradoBudget;
            totalPresupuestado += saldoBudget + cobradoBudget;
        }

        return new DtoManagementResumenFinanciero(idManagement, totalPresupuestado, totalCobrado, saldoPending);
    }
}
