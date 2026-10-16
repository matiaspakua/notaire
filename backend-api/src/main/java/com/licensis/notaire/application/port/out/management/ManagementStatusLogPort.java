package com.licensis.notaire.application.port.out.management;

/**
 * Outbound port for recording management status transitions in the audit trail (issue #1850).
 */
public interface ManagementStatusLogPort {

    /**
     * Register a status change in the management's history.
     *
     * @param managementId the management after transition
     * @param detail optional detail for the status change
     */
    void registerStatus(Integer managementId, String detail);
}
