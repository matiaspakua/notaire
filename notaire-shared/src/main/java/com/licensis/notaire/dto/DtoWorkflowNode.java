package com.licensis.notaire.dto;

public class DtoWorkflowNode {

    private Integer id;
    private Integer workflowDefinitionId;
    private Integer estadoGestionId;
    private String estadoGestionNombre;
    private String tipo;
    private Float posicionX;
    private Float posicionY;
    private Integer version = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWorkflowDefinitionId() {
        return workflowDefinitionId;
    }

    public void setWorkflowDefinitionId(Integer workflowDefinitionId) {
        this.workflowDefinitionId = workflowDefinitionId;
    }

    public Integer getEstadoGestionId() {
        return estadoGestionId;
    }

    public void setEstadoGestionId(Integer estadoGestionId) {
        this.estadoGestionId = estadoGestionId;
    }

    public String getEstadoGestionNombre() {
        return estadoGestionNombre;
    }

    public void setEstadoGestionNombre(String estadoGestionNombre) {
        this.estadoGestionNombre = estadoGestionNombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Float getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(Float posicionX) {
        this.posicionX = posicionX;
    }

    public Float getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(Float posicionY) {
        this.posicionY = posicionY;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
