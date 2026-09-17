package com.licensis.notaire.application.port.out.management;

import com.licensis.notaire.business.ManagementStatus;

/**
 * Outbound port for looking up management statuses.
 */
public interface StatusRepositoryPort {

    /**
     * Find a status by its name.
     *
     * @param statusName the status name
     * @return the status, or null if not found
     */
    ManagementStatus findByName(String statusName);
}
