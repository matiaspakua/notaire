package com.licensis.notaire.exception;

/**
 * Thrown by {@code PagoService.procesarPago} when a payment's monto exceeds the saldo pendiente
 * of the presupuesto it applies to (CU15, Issue #848). Intentionally NOT a {@link NotaireException}
 * subclass: {@code PagoController}'s local try/catch blocks map this directly to HTTP 409, so it
 * does not need {@code GlobalExceptionHandler} dispatch or the {@link ErrorResponse} body shape.
 */
public class SaldoPendienteExcedidoException extends RuntimeException {

    public SaldoPendienteExcedidoException(String message) {
        super(message);
    }
}
