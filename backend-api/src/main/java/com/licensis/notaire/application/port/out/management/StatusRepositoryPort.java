package com.licensis.notaire.application.port.out.management;

import com.licensis.notaire.domain.management.ManagementStatus;

/**
 * Outbound port for looking up management statuses as domain views (issue #1850).
 */
public interface StatusRepositoryPort {

    /**
     * Find a status by its name.
     *
     * @param statusName the status name
     * @return the domain status, or null if not found
     */
    ManagementStatus findByName(String statusName);
}
