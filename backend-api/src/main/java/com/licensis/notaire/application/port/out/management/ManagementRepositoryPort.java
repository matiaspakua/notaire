package com.licensis.notaire.application.port.out.management;

import com.licensis.notaire.domain.management.ManagementTransitionSnapshot;

/**
 * Outbound port for persisting and retrieving management transition state (issue #1850).
 */
public interface ManagementRepositoryPort {

    /**
     * Retrieve a management transition snapshot by ID.
     *
     * @param managementId the management ID
     * @return the snapshot, or null if not found
     */
    ManagementTransitionSnapshot findById(Integer managementId);

    /**
     * Persist a new status on the management and return the updated snapshot.
     *
     * @param managementId the management ID
     * @param statusId the destination status ID
     * @return the saved snapshot
     */
    ManagementTransitionSnapshot saveStatus(Integer managementId, Integer statusId);
}
