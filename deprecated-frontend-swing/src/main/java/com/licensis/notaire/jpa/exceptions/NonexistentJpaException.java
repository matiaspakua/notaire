package com.licensis.notaire.jpa.exceptions;

/**
 * Excepción stub para compatibilidad con formularios.
 */
public class NonexistentJpaException extends Exception {
    private static final long serialVersionUID = 1L;
    public NonexistentJpaException() { super(); }
    public NonexistentJpaException(String message) { super(message); }
    public NonexistentJpaException(Throwable cause) { super(cause); }
}
