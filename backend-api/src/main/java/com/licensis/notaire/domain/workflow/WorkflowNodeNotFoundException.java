package com.licensis.notaire.domain.workflow;

/** Raised when a workflow node id is absent (issue #1729). */
public class WorkflowNodeNotFoundException extends RuntimeException {

    public WorkflowNodeNotFoundException(int id) {
        super("Workflow node not found: " + id);
    }
}
