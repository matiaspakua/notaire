package com.licensis.notaire.service.mappers;

import com.licensis.notaire.dto.DtoPaymentResponse;
import com.licensis.notaire.business.Payment;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static DtoPaymentResponse toDto(Payment payment) {
        return new DtoPaymentResponse(
                payment.getIdPayment(),
                payment.getBudget() != null ? payment.getBudget().getIdBudget() : null,
                payment.getAmount(),
                payment.getDate(),
                payment.getPaymentMethod(),
                payment.getNotes()
        );
    }
}
