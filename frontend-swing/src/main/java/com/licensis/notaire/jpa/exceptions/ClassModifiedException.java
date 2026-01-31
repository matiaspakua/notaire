package com.licensis.notaire.jpa.exceptions;

/**
 * Excepción stub para compatibilidad con formularios que la referencian.
 * En frontend REST no se usa JPA; se reemplaza por errores de API.
 */
public class ClassModifiedException extends Exception {
    private static final long serialVersionUID = 1L;
    public ClassModifiedException() { super(); }
    public ClassModifiedException(String message) { super(message); }
    public ClassModifiedException(Throwable cause) { super(cause); }
}
