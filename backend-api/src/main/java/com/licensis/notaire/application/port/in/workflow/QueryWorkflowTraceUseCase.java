package com.licensis.notaire.application.port.in.workflow;

import com.licensis.notaire.dto.DtoManagementWorkflowTrace;

/**
 * Inbound port for building a management workflow trace (issue #1851).
 */
public interface QueryWorkflowTraceUseCase {

    /**
     * Builds the aggregated workflow trace for a given management.
     *
     * @param managementId the management ID
     * @return the workflow trace DTO
     */
    DtoManagementWorkflowTrace buildTrace(Integer managementId);
}
