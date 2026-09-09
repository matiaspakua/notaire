package com.licensis.notaire.dto;

import java.util.Date;

/**
 * Read-model for gestión list/detail endpoints. Avoids serializing the JPA
 * entity, whose lazy collections (tramiteList, historialList) and circular
 * back-references cannot be written outside a Hibernate session.
 */
public record DtoManagementSummary(
        Integer idManagement,
        int number,
        String encabezado,
        Date dateStart,
        String statusActual,
        int procedureCount,
        String notes) {
}
