package com.licensis.notaire.domain.workflow;

/** Raised when a workflow transition id is absent (issue #1730). */
public class WorkflowTransitionNotFoundException extends RuntimeException {

    public WorkflowTransitionNotFoundException(int id) {
        super("Workflow transition not found: " + id);
    }
}
