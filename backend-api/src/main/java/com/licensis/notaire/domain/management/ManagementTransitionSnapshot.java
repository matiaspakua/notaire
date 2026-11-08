package com.licensis.notaire.domain.management;

/**
 * Read model for CU83 management status transition. Application ports speak this view instead of
 * JPA {@code DeedManagement} (issue #1850 / ADR-030).
 */
public record ManagementTransitionSnapshot(
        Integer idManagement, Integer statusId, String statusName, Integer workflowDefinitionId) {}
