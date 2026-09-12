package com.licensis.notaire.exception;

/**
 * Exception thrown when a persona is created or updated with a document
 * (tipo + numero de identificacion) that already belongs to another persona.
 */
public class DuplicatePersonException extends NotaireException {

    private final Integer idPersonExistente;

    public DuplicatePersonException(String message, Integer idPersonExistente) {
        super(409, message);
        this.idPersonExistente = idPersonExistente;
    }

    public Integer getIdPersonExistente() {
        return idPersonExistente;
    }
}
