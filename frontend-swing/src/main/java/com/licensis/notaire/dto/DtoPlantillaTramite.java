package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

public class DtoPlantillaTramite implements DtoValido {
    private Integer version;

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
