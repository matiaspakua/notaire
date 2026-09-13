package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.DeletePaymentUseCase;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Removes a registered payment (CU15).
 */
@Service
public class DeletePaymentService implements DeletePaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeletePaymentService.class);

    private final PaymentRepositoryPort payments;

    public DeletePaymentService(PaymentRepositoryPort payments) {
        this.payments = payments;
    }

    @Override
    @Transactional
    public void delete(Integer paymentId) {
        log.info("Eliminando pago con ID: {}", paymentId);
        if (!payments.existsById(paymentId)) {
            throw new IllegalArgumentException("Pago no encontrado con ID: " + paymentId);
        }
        payments.deleteById(paymentId);
        log.info("Pago eliminado exitosamente: ID={}", paymentId);
    }
}
