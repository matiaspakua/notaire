package com.licensis.notaire.adapter.in.web.support;

public record DocumentTypeRef(
        Integer idDocumentType,
        String name,
        boolean expires,
        Integer dueDays,
        String deliveredBy,
        boolean returned,
        boolean enabled) {}
