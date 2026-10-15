package com.licensis.notaire.adapter.in.web.management;

import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementOutput;

/**
 * Web adapter mapper for TransitionManagement use case.
 * Maps between HTTP contracts and use case I/O via the query port (issue #1772).
 */
public class TransitionManagementWebMapper {

    private final QueryManagementsUseCase queryManagements;

    public TransitionManagementWebMapper(QueryManagementsUseCase queryManagements) {
        this.queryManagements = queryManagements;
    }

    /**
     * Maps the use case output to the HTTP response DTO.
     */
    public Object mapToHttpResponse(TransitionManagementOutput output) {
        String statusName = queryManagements.findCurrentStatusName(output.managementId())
                .orElseThrow(() -> new IllegalStateException(
                        "Management should exist after transition: " + output.managementId()));
        return new ManagementSummaryDto(output.managementId(), statusName);
    }

    /**
     * Simple DTO for HTTP response payload.
     */
    public record ManagementSummaryDto(Integer id, String currentStatus) {
    }
}
