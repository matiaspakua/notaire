package com.licensis.notaire.adapter.in.web.payment;

import com.licensis.notaire.application.port.in.payment.BudgetSummary;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.dto.DtoBudgetResumen;
import com.licensis.notaire.dto.DtoPaymentResponse;

import java.util.List;

/**
 * Maps payment use-case results onto the REST response DTOs.
 *
 * <p>Mapping lives in the inbound adapter, never inside a use case: the application
 * layer speaks domain records, and only this edge knows the wire format.
 *
 * <p>Public only as a transitional seam (ADR-021): {@code BudgetController} still lives
 * in the legacy {@code api} package while it consumes {@code GetBudgetSummaryUseCase}.
 * It becomes package-private again once the budget slice is migrated.
 */
public final class PaymentWebMapper {

    private PaymentWebMapper() {
    }

    public static DtoPaymentResponse toDto(PaymentDetails payment) {
        return new DtoPaymentResponse(
                payment.id(),
                payment.budgetId(),
                payment.amount(),
                payment.date(),
                payment.paymentMethod(),
                payment.notes());
    }

    public static List<DtoPaymentResponse> toDtoList(List<PaymentDetails> payments) {
        return payments.stream().map(PaymentWebMapper::toDto).toList();
    }

    public static DtoBudgetResumen toDto(BudgetSummary summary) {
        return new DtoBudgetResumen(
                summary.budgetId(),
                summary.budgetNumber(),
                summary.managementId(),
                summary.managementNumber(),
                summary.managementHeading(),
                summary.total(),
                summary.pendingBalance(),
                toDtoList(summary.payments()));
    }
}
