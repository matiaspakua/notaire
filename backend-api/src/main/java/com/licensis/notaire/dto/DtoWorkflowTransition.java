package com.licensis.notaire.dto;

public class DtoWorkflowTransition {

    private Integer id;
    private Integer workflowDefinitionId;
    private Integer originNodeId;
    private Integer destinationNodeId;
    private String condition;
    private String description;
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

    public Integer getOriginNodeId() {
        return originNodeId;
    }

    public void setOriginNodeId(Integer originNodeId) {
        this.originNodeId = originNodeId;
    }

    public Integer getDestinationNodeId() {
        return destinationNodeId;
    }

    public void setDestinationNodeId(Integer destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
