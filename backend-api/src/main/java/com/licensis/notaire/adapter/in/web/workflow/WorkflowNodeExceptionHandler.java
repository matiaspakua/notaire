package com.licensis.notaire.adapter.in.web.workflow;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.workflow.WorkflowNodeInUseException;
import com.licensis.notaire.domain.workflow.WorkflowNodeNotFoundException;

/**
 * Maps workflow-node domain exceptions to the HTTP shapes clients already get (#577).
 */
@RestControllerAdvice(assignableTypes = WorkflowNodeController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class WorkflowNodeExceptionHandler {

    @ExceptionHandler(WorkflowNodeNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(WorkflowNodeInUseException.class)
    ResponseEntity<Map<String, String>> inUse(WorkflowNodeInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", locked.getMessage()));
    }
}
