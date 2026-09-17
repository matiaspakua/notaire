package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.EditPaymentCommand;
import com.licensis.notaire.application.port.in.payment.EditPaymentUseCase;
import com.licensis.notaire.application.port.out.payment.PaymentChanges;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import com.licensis.notaire.domain.payment.PaymentDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies a partial edit to an existing payment (CU15).
 */
@Service
public class EditPaymentService implements EditPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(EditPaymentService.class);

    private final PaymentRepositoryPort payments;

    public EditPaymentService(PaymentRepositoryPort payments) {
        this.payments = payments;
    }

    @Override
    @Transactional
    public PaymentDetails edit(EditPaymentCommand command) {
        Integer paymentId = command.paymentId();
        log.info("Editando pago con ID: {}", paymentId);

        if (!payments.existsById(paymentId)) {
            throw new IllegalArgumentException("Pago no encontrado con ID: " + paymentId);
        }

        Float amount = command.amount();
        if (amount != null && amount <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }

        PaymentDetails updated = payments.applyChanges(paymentId, new PaymentChanges(
                amount, command.date(), command.notes(), command.paymentMethod()));

        log.info("Pago editado exitosamente: ID={}", updated.id());
        return updated;
    }
}
