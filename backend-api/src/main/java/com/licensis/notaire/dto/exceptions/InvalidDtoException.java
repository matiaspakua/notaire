package com.licensis.notaire.dto.exceptions;

/**
 *
 * @author matias
 */
public class InvalidDtoException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidDtoException(String message) {
        super(message);
    }

    public InvalidDtoException() {
    }
}
