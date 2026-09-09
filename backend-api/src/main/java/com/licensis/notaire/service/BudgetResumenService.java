package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoBudgetResumen;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.service.mappers.PaymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU47 - Consultar Pago: resumen financiero de un presupuesto (total, saldo
 * pendiente y pagos aplicados), junto con la gestión a la que pertenece.
 */
@Service
public class BudgetResumenService {

    private final BudgetRepository budgetRepository;
    private final ProcedureRepository procedureRepository;
    private final PaymentService paymentService;

    public BudgetResumenService(BudgetRepository budgetRepository,
            ProcedureRepository procedureRepository, PaymentService paymentService) {
        this.budgetRepository = budgetRepository;
        this.procedureRepository = procedureRepository;
        this.paymentService = paymentService;
    }

    @Transactional(readOnly = true)
    public DtoBudgetResumen obtenerResumen(Integer idBudget) {
        Budget budget = budgetRepository.findById(idBudget)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idBudget));

        Float saldoPending = paymentService.calcularSaldoPending(idBudget);
        var payments = paymentService.findPaymentsByBudget(idBudget);
        float totalPagado = (float) payments.stream().mapToDouble(p -> p.getAmount()).sum();
        Float total = saldoPending + totalPagado;

        List<Procedure> procedures = procedureRepository.findByFkIdBudgetIdBudget(idBudget);
        Procedure procedure = procedures.isEmpty() ? null : procedures.get(0);
        var management = procedure != null ? procedure.getFkIdManagement() : null;

        return new DtoBudgetResumen(
                budget.getIdBudget(),
                budget.getNumber(),
                management != null ? management.getIdManagement() : null,
                management != null ? management.getNumber() : null,
                management != null ? management.getEncabezado() : null,
                total,
                saldoPending,
                payments.stream().map(PaymentMapper::toDto).toList());
    }
}
