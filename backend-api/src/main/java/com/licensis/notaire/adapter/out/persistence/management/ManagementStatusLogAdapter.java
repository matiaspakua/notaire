package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.ManagementStatusLogPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.application.usecase.management.ManagementStatusLogService;

/**
 * Outbound adapter for ManagementStatusLogPort.
 * Adapts the existing ManagementStatusLogService to the port interface.
 */
public class ManagementStatusLogAdapter implements ManagementStatusLogPort {

    private final ManagementStatusLogService statusLogService;

    public ManagementStatusLogAdapter(ManagementStatusLogService statusLogService) {
        this.statusLogService = statusLogService;
    }

    @Override
    public void registerStatus(DeedManagement management, String detail) {
        statusLogService.registerStatus(management, detail);
    }
}
