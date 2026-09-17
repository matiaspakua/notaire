package com.licensis.notaire.application.port.out.management;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.WorkflowDefinition;

/**
 * Outbound port for resolving the workflow definition for a management.
 */
public interface WorkflowLookupPort {

    /**
     * Resolve the workflow definition for a management based on its associated procedures.
     *
     * @param management the management entity
     * @return the workflow definition, or null if not found
     */
    WorkflowDefinition resolveWorkflowDefinition(DeedManagement management);
}
