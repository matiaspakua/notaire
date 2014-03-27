/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa.exceptions;

/**
 *
 * @author matias
 */
public class CreateEntityException extends Exception
{

    private static final long serialVersionUID = 1L;

    public CreateEntityException() {
    }

    public CreateEntityException(String message) {
        super(message);
    }

    public CreateEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateEntityException(Throwable cause) {
        super(cause);
    }

    public CreateEntityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
