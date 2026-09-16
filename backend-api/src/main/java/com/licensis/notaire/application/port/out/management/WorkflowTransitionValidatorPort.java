package com.licensis.notaire.application.port.out.management;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;

/**
 * Outbound port for validating if a transition is allowed by the workflow definition.
 */
public interface WorkflowTransitionValidatorPort {

    /**
     * Validate that a transition exists from origin to destination status according to the
     * workflow definition.
     *
     * @param workflowDefinition the workflow definition to check against
     * @param origin the origin status
     * @param destination the destination status
     * @return true if transition is valid, false otherwise
     */
    boolean isTransitionValid(WorkflowDefinition workflowDefinition, ManagementStatus origin,
            ManagementStatus destination);
}
