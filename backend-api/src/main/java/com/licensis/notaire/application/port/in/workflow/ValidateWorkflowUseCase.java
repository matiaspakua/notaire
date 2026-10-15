package com.licensis.notaire.application.port.in.workflow;

import java.util.List;

/**
 * Validates workflow graph consistency (CU72 / issue #1770).
 */
public interface ValidateWorkflowUseCase {

    Result validate(int workflowDefinitionId);

    sealed interface Result {
        record NotFound() implements Result {}

        record Ok(boolean valid, List<String> errors) implements Result {}
    }
}
