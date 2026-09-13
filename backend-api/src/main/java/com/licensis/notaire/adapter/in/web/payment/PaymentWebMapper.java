package com.licensis.notaire.adapter.in.web.payment;

import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.dto.DtoPaymentResponse;

import java.util.List;

/**
 * Maps payment use-case results onto the REST response DTOs.
 *
 * <p>Mapping lives in the inbound adapter, never inside a use case: the application
 * layer speaks domain records, and only this edge knows the wire format.
 */
final class PaymentWebMapper {

    private PaymentWebMapper() {
    }

    static DtoPaymentResponse toDto(PaymentDetails payment) {
        return new DtoPaymentResponse(
                payment.id(),
                payment.budgetId(),
                payment.amount(),
                payment.date(),
                payment.paymentMethod(),
                payment.notes());
    }

    static List<DtoPaymentResponse> toDtoList(List<PaymentDetails> payments) {
        return payments.stream().map(PaymentWebMapper::toDto).toList();
    }
}
