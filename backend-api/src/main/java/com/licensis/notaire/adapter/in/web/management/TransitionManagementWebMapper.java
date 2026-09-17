package com.licensis.notaire.adapter.in.web.management;

import com.licensis.notaire.application.port.in.management.TransitionManagementOutput;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.repository.DeedManagementRepository;

/**
 * Web adapter mapper for TransitionManagement use case.
 * Maps between HTTP contracts and use case I/O, and fetches additional data for the response.
 */
public class TransitionManagementWebMapper {

    private final DeedManagementRepository managementRepository;

    public TransitionManagementWebMapper(DeedManagementRepository managementRepository) {
        this.managementRepository = managementRepository;
    }

    /**
     * Maps the use case output to the HTTP response DTO.
     * Fetches the full management summary to return to the client.
     */
    public Object mapToHttpResponse(TransitionManagementOutput output) {
        DeedManagement management = managementRepository.findById(output.managementId())
                .orElseThrow(() -> new IllegalStateException(
                        "Management should exist after transition: " + output.managementId()));
        return new ManagementSummaryDto(management.getIdManagement(),
                management.getFkIdManagementStatus().getName());
    }

    /**
     * Simple DTO for HTTP response payload.
     */
    public record ManagementSummaryDto(Integer id, String statusActual) {
    }
}
