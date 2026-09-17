package com.licensis.notaire.application.port.in.management;

/**
 * Inbound port for CU83 - Transición de estado de gestión.
 * Defines the contract for transitioning a management's status against its workflow definition.
 */
public interface TransitionManagementUseCase {

    /**
     * Validates that a transition exists from the management's current status to the destination
     * status according to its workflow definition, and applies it if valid.
     *
     * @param managementId the ID of the management to transition
     * @param statusDestination the name of the destination status
     * @return a DTO with the updated management summary
     * @throws com.licensis.notaire.exception.ResourceNotFoundException if management not found
     * @throws com.licensis.notaire.exception.BusinessValidationException if transition invalid
     */
    TransitionManagementOutput execute(Integer managementId, String statusDestination);
}
