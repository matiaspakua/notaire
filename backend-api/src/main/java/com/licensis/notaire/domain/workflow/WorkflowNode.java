package com.licensis.notaire.domain.workflow;

/**
 * A workflow node (status step on a workflow definition). Plain Java (issue #1729, ADR-030).
 */
public record WorkflowNode(
        Integer id,
        Integer workflowDefinitionId,
        Integer statusManagementId,
        String statusManagementName,
        WorkflowNodeKind kind,
        Float positionX,
        Float positionY,
        int version) {

    public static WorkflowNode create(
            Integer workflowDefinitionId,
            Integer statusManagementId,
            String statusManagementName,
            WorkflowNodeKind kind,
            Float positionX,
            Float positionY) {
        return new WorkflowNode(
                null, workflowDefinitionId, statusManagementId, statusManagementName, kind, positionX, positionY, 0);
    }

    public static WorkflowNode restore(
            Integer id,
            Integer workflowDefinitionId,
            Integer statusManagementId,
            String statusManagementName,
            WorkflowNodeKind kind,
            Float positionX,
            Float positionY,
            int version) {
        return new WorkflowNode(
                id, workflowDefinitionId, statusManagementId, statusManagementName, kind, positionX, positionY, version);
    }

    public WorkflowNode changeTo(WorkflowNodeKind newKind, Float newPositionX, Float newPositionY) {
        return new WorkflowNode(
                id,
                workflowDefinitionId,
                statusManagementId,
                statusManagementName,
                newKind != null ? newKind : kind,
                newPositionX != null ? newPositionX : positionX,
                newPositionY != null ? newPositionY : positionY,
                version);
    }

    public WorkflowNode withIdentity(Integer newId, int newVersion) {
        return new WorkflowNode(
                newId,
                workflowDefinitionId,
                statusManagementId,
                statusManagementName,
                kind,
                positionX,
                positionY,
                newVersion);
    }
}
