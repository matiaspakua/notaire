/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

/**
 *
 * @author juanca
 */
public class DtoFlag implements DtoValido
{

    boolean flag = false;

    public DtoFlag()
    {
    }

    public Boolean getFlag()
    {
        return flag;
    }

    public void setFlag(Boolean flag)
    {
        this.flag = flag;
    }

    @Override
    public Boolean isValido()
    {
        //  TODO: Implementar.
        return true;
    }
}
