package com.licensis.notaire.domain.workflow;

/**
 * A workflow transition (edge between two nodes on a workflow definition). Plain Java (issue #1730,
 * ADR-030).
 */
public record WorkflowTransition(
        Integer id,
        Integer workflowDefinitionId,
        Integer originNodeId,
        Integer destinationNodeId,
        String condition,
        String description,
        int version) {

    public static WorkflowTransition create(
            Integer workflowDefinitionId,
            Integer originNodeId,
            Integer destinationNodeId,
            String condition,
            String description) {
        return new WorkflowTransition(
                null, workflowDefinitionId, originNodeId, destinationNodeId, condition, description, 0);
    }

    public static WorkflowTransition restore(
            Integer id,
            Integer workflowDefinitionId,
            Integer originNodeId,
            Integer destinationNodeId,
            String condition,
            String description,
            int version) {
        return new WorkflowTransition(
                id, workflowDefinitionId, originNodeId, destinationNodeId, condition, description, version);
    }

    public WorkflowTransition changeTo(
            Integer newOriginNodeId,
            Integer newDestinationNodeId,
            String newCondition,
            String newDescription) {
        return new WorkflowTransition(
                id,
                workflowDefinitionId,
                newOriginNodeId != null ? newOriginNodeId : originNodeId,
                newDestinationNodeId != null ? newDestinationNodeId : destinationNodeId,
                newCondition,
                newDescription,
                version);
    }

    public WorkflowTransition withIdentity(Integer newId, int newVersion) {
        return new WorkflowTransition(
                newId, workflowDefinitionId, originNodeId, destinationNodeId, condition, description, newVersion);
    }
}
