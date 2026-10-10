package com.licensis.notaire.application.usecase.management;

import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.domain.payment.Money;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.dto.DtoManagementFinancialSummary;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CU47/CU02 - Financial summary agregado of a management: total presupuestado,
 * total cobrado and outstanding balance, sumados a través of the budgets of
 * each procedure vinculado.
 */
@Service
public class ManagementResumenFinancieroService {

    private final ProcedureRepository procedureRepository;
    private final GetPaymentStatusUseCase paymentStatus;
    private final QueryPaymentsUseCase paymentQueries;
    private final ManagementArchiveDebtService managementArchiveDebtService;

    public ManagementResumenFinancieroService(ProcedureRepository procedureRepository,
            GetPaymentStatusUseCase paymentStatus, QueryPaymentsUseCase paymentQueries,
            ManagementArchiveDebtService managementArchiveDebtService) {
        this.procedureRepository = procedureRepository;
        this.paymentStatus = paymentStatus;
        this.paymentQueries = paymentQueries;
        this.managementArchiveDebtService = managementArchiveDebtService;
    }

    @Transactional(readOnly = true)
    public DtoManagementFinancialSummary getSummary(Integer idManagement) {
        BigDecimal pendingBalance = managementArchiveDebtService.calculatePendingBalance(idManagement);

        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(idManagement);
        Set<Integer> idsBudgetContados = new HashSet<>();
        BigDecimal totalPresupuestado = Money.zero();
        BigDecimal totalCobrado = Money.zero();

        for (Procedure procedure : procedures) {
            Budget budget = procedure.getFkIdBudget();
            if (budget == null || !idsBudgetContados.add(budget.getIdBudget())) {
                continue;
            }
            BigDecimal saldoBudget = paymentStatus.pendingBalance(budget.getIdBudget());
            BigDecimal cobradoBudget = paymentQueries.findByBudget(budget.getIdBudget()).stream()
                    .map(PaymentDetails::amount)
                    .map(Money::nullToZero)
                    .reduce(Money.zero(), BigDecimal::add);
            totalCobrado = Money.of(totalCobrado.add(cobradoBudget));
            totalPresupuestado = Money.of(totalPresupuestado.add(saldoBudget).add(cobradoBudget));
        }

        return new DtoManagementFinancialSummary(idManagement, totalPresupuestado, totalCobrado, pendingBalance);
    }
}
