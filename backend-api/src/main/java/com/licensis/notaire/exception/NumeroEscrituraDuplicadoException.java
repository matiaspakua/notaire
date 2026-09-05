package com.licensis.notaire.exception;

/**
 * Thrown when the número proposed for an escritura is already used by another
 * escritura of the same protocolo, año and escribano (CU86, excepción 3.1).
 */
public class NumeroEscrituraDuplicadoException extends NotaireException {

    public NumeroEscrituraDuplicadoException(String message) {
        super(409, message);
    }
}
