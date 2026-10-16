package com.licensis.notaire.adapter.in.web.support;

public record ProcedureTypeRef(
        Integer idProcedureType,
        String name,
        String notes,
        boolean isArchived,
        boolean isRegistered,
        boolean associatesProperties,
        boolean enabled) {}
