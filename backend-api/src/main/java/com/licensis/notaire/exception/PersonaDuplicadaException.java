package com.licensis.notaire.exception;

/**
 * Exception thrown when a persona is created or updated with a document
 * (tipo + numero de identificacion) that already belongs to another persona.
 */
public class PersonaDuplicadaException extends NotaireException {

    private final Integer idPersonaExistente;

    public PersonaDuplicadaException(String message, Integer idPersonaExistente) {
        super(409, message);
        this.idPersonaExistente = idPersonaExistente;
    }

    public Integer getIdPersonaExistente() {
        return idPersonaExistente;
    }
}
