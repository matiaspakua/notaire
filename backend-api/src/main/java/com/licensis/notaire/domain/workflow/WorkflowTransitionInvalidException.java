package com.licensis.notaire.domain.workflow;

/** Raised when create/update references are missing (issue #1730). */
public class WorkflowTransitionInvalidException extends RuntimeException {

    public WorkflowTransitionInvalidException(String message) {
        super(message);
    }

    public static WorkflowTransitionInvalidException missingWorkflow() {
        return new WorkflowTransitionInvalidException("workflow definition not found");
    }

    public static WorkflowTransitionInvalidException missingOriginNode() {
        return new WorkflowTransitionInvalidException("origin node not found");
    }

    public static WorkflowTransitionInvalidException missingDestinationNode() {
        return new WorkflowTransitionInvalidException("destination node not found");
    }
}
