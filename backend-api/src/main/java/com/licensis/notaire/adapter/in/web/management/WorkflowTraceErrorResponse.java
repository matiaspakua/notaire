package com.licensis.notaire.adapter.in.web.management;

/** JSON body for expected workflow-trace 400 responses — same keys clients already consume (#577). */
public record WorkflowTraceErrorResponse(String error) {
}
