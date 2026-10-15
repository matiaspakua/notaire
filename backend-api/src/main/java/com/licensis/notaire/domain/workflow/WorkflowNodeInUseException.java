package com.licensis.notaire.domain.workflow;

/** Raised when a workflow node cannot be deleted because it has transitions (issue #1729). */
public class WorkflowNodeInUseException extends RuntimeException {

    public static final String DELETE_MESSAGE = "Cannot delete: the node has associated transitions.";

    public WorkflowNodeInUseException(String message) {
        super(message);
    }

    public static WorkflowNodeInUseException cannotDelete() {
        return new WorkflowNodeInUseException(DELETE_MESSAGE);
    }
}
