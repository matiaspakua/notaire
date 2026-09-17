package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.ManagementBitacoraPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.service.ManagementBitacoraService;

/**
 * Outbound adapter for ManagementBitacoraPort.
 * Adapts the existing ManagementBitacoraService to the port interface.
 */
public class ManagementBitacoraAdapter implements ManagementBitacoraPort {

    private final ManagementBitacoraService managementBitacoraService;

    public ManagementBitacoraAdapter(ManagementBitacoraService managementBitacoraService) {
        this.managementBitacoraService = managementBitacoraService;
    }

    @Override
    public void registerStatus(DeedManagement management, String detail) {
        managementBitacoraService.registerStatus(management, detail);
    }
}
