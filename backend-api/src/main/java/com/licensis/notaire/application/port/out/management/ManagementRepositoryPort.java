package com.licensis.notaire.application.port.out.management;

import com.licensis.notaire.business.DeedManagement;

/**
 * Outbound port for persisting and retrieving management entities.
 */
public interface ManagementRepositoryPort {

    /**
     * Retrieve a management by ID.
     *
     * @param managementId the management ID
     * @return the management, or null if not found
     */
    DeedManagement findById(Integer managementId);

    /**
     * Save (persist or update) a management entity.
     *
     * @param management the management to save
     * @return the saved management
     */
    DeedManagement save(DeedManagement management);
}
