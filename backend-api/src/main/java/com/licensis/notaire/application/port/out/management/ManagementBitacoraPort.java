package com.licensis.notaire.application.port.out.management;

import com.licensis.notaire.business.DeedManagement;

/**
 * Outbound port for recording management status transitions in the audit trail.
 */
public interface ManagementBitacoraPort {

    /**
     * Register a status change in the management's history.
     *
     * @param management the management after transition
     * @param detail optional detail for the status change
     */
    void registerStatus(DeedManagement management, String detail);
}
