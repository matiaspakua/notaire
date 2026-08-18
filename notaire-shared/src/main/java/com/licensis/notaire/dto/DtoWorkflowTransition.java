package com.licensis.notaire.dto;

public class DtoWorkflowTransition {

    private Integer id;
    private Integer workflowDefinitionId;
    private Integer nodoOrigenId;
    private Integer nodoDestinoId;
    private String condicion;
    private String descripcion;
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

    public Integer getNodoOrigenId() {
        return nodoOrigenId;
    }

    public void setNodoOrigenId(Integer nodoOrigenId) {
        this.nodoOrigenId = nodoOrigenId;
    }

    public Integer getNodoDestinoId() {
        return nodoDestinoId;
    }

    public void setNodoDestinoId(Integer nodoDestinoId) {
        this.nodoDestinoId = nodoDestinoId;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
