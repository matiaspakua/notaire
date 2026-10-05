package com.licensis.notaire.dto;

public class DtoWorkflowNode {

    private Integer id;
    private Integer workflowDefinitionId;
    private Integer statusManagementId;
    private String statusManagementName;
    private String type;
    private Float positionX;
    private Float positionY;
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

    public Integer getStatusManagementId() {
        return statusManagementId;
    }

    public void setStatusManagementId(Integer statusManagementId) {
        this.statusManagementId = statusManagementId;
    }

    public String getStatusManagementName() {
        return statusManagementName;
    }

    public void setStatusManagementName(String statusManagementName) {
        this.statusManagementName = statusManagementName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Float getPositionX() {
        return positionX;
    }

    public void setPositionX(Float positionX) {
        this.positionX = positionX;
    }

    public Float getPositionY() {
        return positionY;
    }

    public void setPositionY(Float positionY) {
        this.positionY = positionY;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
