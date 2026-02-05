package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

/**
 * DTO que representa un flag booleano
 *
 * @author juanca
 */
public class DtoFlag implements DtoValido {

    boolean flag = false;

    public DtoFlag() {
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }
}
