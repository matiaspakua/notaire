package com.licensis.notaire.application.port.out.management;

/**
 * Outbound port for automatic management status transitions triggered by other slices
 * (e.g. external-entity documents). Speaks ids and status names only (issue #1850).
 */
public interface ManagementAutoTransitionPort {

    /**
     * Attempt a status transition for the given management.
     *
     * @param managementId the management ID
     * @param destinationStatusName the destination status name
     */
    void transition(Integer managementId, String destinationStatusName);
}
