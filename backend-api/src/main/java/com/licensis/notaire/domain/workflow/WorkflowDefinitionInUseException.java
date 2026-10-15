package com.licensis.notaire.domain.workflow;

/** Raised when a workflow definition cannot be deleted because it has nodes (issue #1728). */
public class WorkflowDefinitionInUseException extends RuntimeException {

    public static final String DELETE_MESSAGE = "Cannot delete: the workflow has associated nodes.";

    public WorkflowDefinitionInUseException(String message) {
        super(message);
    }

    public static WorkflowDefinitionInUseException cannotDelete() {
        return new WorkflowDefinitionInUseException(DELETE_MESSAGE);
    }
}
