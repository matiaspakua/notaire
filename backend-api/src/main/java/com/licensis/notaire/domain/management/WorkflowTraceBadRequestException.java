package com.licensis.notaire.domain.management;

/** Expected workflow-trace validation failure mapped to HTTP 400 by {@code ManagementWorkflowTraceExceptionHandler}. */
public class WorkflowTraceBadRequestException extends RuntimeException {

    public WorkflowTraceBadRequestException(String message) {
        super(message);
    }
}
