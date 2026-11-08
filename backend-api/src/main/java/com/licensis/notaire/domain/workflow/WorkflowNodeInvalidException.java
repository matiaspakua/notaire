package com.licensis.notaire.domain.workflow;

/** Raised when create/update references are missing (issue #1729). */
public class WorkflowNodeInvalidException extends RuntimeException {

    public WorkflowNodeInvalidException(String message) {
        super(message);
    }

    public static WorkflowNodeInvalidException missingWorkflow() {
        return new WorkflowNodeInvalidException("workflow definition not found");
    }

    public static WorkflowNodeInvalidException missingStatus() {
        return new WorkflowNodeInvalidException("management status not found");
    }
}
