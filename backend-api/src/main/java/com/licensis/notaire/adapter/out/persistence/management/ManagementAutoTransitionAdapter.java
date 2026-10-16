package com.licensis.notaire.adapter.out.persistence.management;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.management.ManagementAutoTransitionPort;

/**
 * Outbound adapter for automatic management transitions used by other application slices
 * (issue #1850).
 */
@Component
public class ManagementAutoTransitionAdapter implements ManagementAutoTransitionPort {

    private final ManagementTransitionService transitionService;

    public ManagementAutoTransitionAdapter(ManagementTransitionService transitionService) {
        this.transitionService = transitionService;
    }

    @Override
    public void transition(Integer managementId, String destinationStatusName) {
        transitionService.transition(managementId, destinationStatusName);
    }
}
