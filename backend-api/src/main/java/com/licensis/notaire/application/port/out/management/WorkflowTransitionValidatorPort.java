package com.licensis.notaire.application.port.out.management;

/**
 * Outbound port for validating if a transition is allowed by the workflow definition (issue #1850).
 */
public interface WorkflowTransitionValidatorPort {

    /**
     * Validate that a transition exists from origin to destination status according to the
     * workflow definition.
     *
     * @param workflowDefinitionId the workflow definition id
     * @param originStatusId the origin management status id
     * @param destinationStatusId the destination management status id
     * @return true if transition is valid, false otherwise
     */
    boolean isTransitionValid(Integer workflowDefinitionId, Integer originStatusId,
            Integer destinationStatusId);
}
