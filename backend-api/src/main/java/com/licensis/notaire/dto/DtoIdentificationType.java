package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;

public class DtoIdentificationType implements DtoValido {
    private Integer idIdentificationType;
    private String name;
    private String characters;
    private Integer version;

    public DtoIdentificationType() {}

    public Integer getIdIdentificationType() { return idIdentificationType; }
    public void setIdIdentificationType(Integer idIdentificationType) { this.idIdentificationType = idIdentificationType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCharacters() { return characters; }
    public void setCharacters(String characters) { this.characters = characters; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
