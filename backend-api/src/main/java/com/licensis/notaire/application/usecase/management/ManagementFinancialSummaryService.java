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
 * CU47/CU02 - Financial summary of a management: total budgeted, total collected,
 * and outstanding balance, summed across the budgets of each linked procedure.
 */
@Service
public class ManagementFinancialSummaryService {

    private final ProcedureRepository procedureRepository;
    private final GetPaymentStatusUseCase paymentStatus;
    private final QueryPaymentsUseCase paymentQueries;
    private final ManagementArchiveDebtService managementArchiveDebtService;

    public ManagementFinancialSummaryService(ProcedureRepository procedureRepository,
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
        Set<Integer> countedBudgetIds = new HashSet<>();
        BigDecimal totalBudgeted = Money.zero();
        BigDecimal totalCollected = Money.zero();

        for (Procedure procedure : procedures) {
            Budget budget = procedure.getFkIdBudget();
            if (budget == null || !countedBudgetIds.add(budget.getIdBudget())) {
                continue;
            }
            BigDecimal outstandingBudget = paymentStatus.pendingBalance(budget.getIdBudget());
            BigDecimal collectedBudget = paymentQueries.findByBudget(budget.getIdBudget()).stream()
                    .map(PaymentDetails::amount)
                    .map(Money::nullToZero)
                    .reduce(Money.zero(), BigDecimal::add);
            totalCollected = Money.of(totalCollected.add(collectedBudget));
            totalBudgeted = Money.of(totalBudgeted.add(outstandingBudget).add(collectedBudget));
        }

        return new DtoManagementFinancialSummary(idManagement, totalBudgeted, totalCollected, pendingBalance);
    }
}
