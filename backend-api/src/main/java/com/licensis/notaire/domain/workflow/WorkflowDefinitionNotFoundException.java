package com.licensis.notaire.domain.workflow;

/** Raised when a workflow definition id is absent (issue #1728). */
public class WorkflowDefinitionNotFoundException extends RuntimeException {

    public WorkflowDefinitionNotFoundException(int id) {
        super("Workflow definition not found: " + id);
    }
}
