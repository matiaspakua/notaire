package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.HashSet;
import java.util.Set;

public class DtoManagementStatus extends GenericDto implements DtoValido {
    private Integer idManagementStatus;
    private String name;
    private String notes;
    private Set historials = new HashSet(0);
    private Integer version;

    public DtoManagementStatus() {
        this.version = DtoValido.VersionINICIAL;
    }

    public DtoManagementStatus(String name) {
        this.name = name;
    }

    public Integer getIdManagementStatus() {
        return idManagementStatus;
    }

    public void setIdManagementStatus(Integer idManagementStatus) {
        this.idManagementStatus = idManagementStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Set getHistorials() {
        return historials;
    }

    public void setHistorials(Set historials) {
        this.historials = historials;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
