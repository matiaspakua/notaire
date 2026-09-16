package com.licensis.notaire.application.port.in.management;

/**
 * Output DTO for TransitionManagementUseCase.
 * Carries the ID of the transitioned management for retrieval by client.
 */
public record TransitionManagementOutput(Integer managementId) {
}
