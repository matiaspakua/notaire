/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa.exceptions;

/**
 *
 * @author matias
 */
public class NonexistentJpaException extends Exception
{

    public NonexistentJpaException(Throwable cause)
    {
        super(cause);
    }

    public NonexistentJpaException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NonexistentJpaException(String message)
    {
        super(message);
    }

    public NonexistentJpaException()
    {
    }
}
