package com.licensis.notaire.adapter.in.web.management;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.management.WorkflowTraceBadRequestException;

/**
 * Maps expected workflow-trace validation failures to the HTTP 400 shape clients already receive (#577).
 */
@RestControllerAdvice(assignableTypes = ManagementController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ManagementWorkflowTraceExceptionHandler {

    @ExceptionHandler(WorkflowTraceBadRequestException.class)
    ResponseEntity<WorkflowTraceErrorResponse> badRequest(WorkflowTraceBadRequestException failure) {
        return ResponseEntity.badRequest().body(new WorkflowTraceErrorResponse(failure.getMessage()));
    }
}
