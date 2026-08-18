package com.licensis.notaire.jpa.exceptions;

/**
 * Excepción stub para compatibilidad con formularios.
 */
public class NonexistentEntityException extends Exception {
    private static final long serialVersionUID = 1L;
    public NonexistentEntityException() { super(); }
    public NonexistentEntityException(String message) { super(message); }
    public NonexistentEntityException(Throwable cause) { super(cause); }
}
