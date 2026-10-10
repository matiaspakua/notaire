package com.licensis.notaire.dto;

import java.util.Date;

/**
 * Read-model for history endpoints. A POJO record without dependencies
 * on backend entity classes; mapping from `ManagementHistory` is performed in the
 * `backend-api` module where the entity type is available.
 */
public record DtoHistorySummary(
        Integer idHistory,
        Date date,
        String notes,
        Integer managementId,
        Integer statusManagementId,
        String statusManagementName) {
}
