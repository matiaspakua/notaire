package com.licensis.notaire.exception;

/**
 * Thrown by {@code ProcessPaymentService} when a payment amount exceeds the outstanding balance
 * of the budget it applies to (CU15, Issue #848). Intentionally NOT a {@link NotaireException}
 * subclass: {@code PaymentController}'s local try/catch blocks map this directly to HTTP 409, so it
 * does not need {@code GlobalExceptionHandler} dispatch or the {@link ErrorResponse} body shape.
 */
public class PendingBalanceExceededException extends RuntimeException {

    public PendingBalanceExceededException(String message) {
        super(message);
    }
}
