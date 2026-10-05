package com.licensis.notaire.dto.exceptions;

/**
 *
 * @author matias
 */
public class DtoInvalidoException extends Exception {

    private static final long serialVersionUID = 1L;

    public DtoInvalidoException(String message) {
        super(message);
    }

    public DtoInvalidoException() {
    }
}
