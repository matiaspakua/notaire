package com.licensis.notaire.application.port.out.management;

/**
 * Outbound port for resolving the workflow definition id for a management (issue #1850).
 */
public interface WorkflowLookupPort {

    /**
     * Resolve the workflow definition id for a management based on its associated procedures.
     *
     * @param managementId the management ID
     * @return the workflow definition id
     */
    Integer resolveWorkflowDefinitionId(Integer managementId);
}
