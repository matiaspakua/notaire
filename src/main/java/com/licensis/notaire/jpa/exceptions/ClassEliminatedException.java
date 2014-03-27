/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa.exceptions;

/**
 *
 * @author User
 */
public class ClassEliminatedException extends Exception
{

    private static final long serialVersionUID = 1L;

    public ClassEliminatedException(String message) {
        super(message);
    }

    public ClassEliminatedException() {
    }
}
