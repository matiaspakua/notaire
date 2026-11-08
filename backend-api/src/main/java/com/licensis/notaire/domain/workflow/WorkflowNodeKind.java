package com.licensis.notaire.domain.workflow;

/** Wire type values for a workflow node (issue #1729). */
public enum WorkflowNodeKind {
    INITIAL,
    INTERMEDIATE,
    FINAL;

    public static WorkflowNodeKind parse(String value) {
        return WorkflowNodeKind.valueOf(value);
    }
}
