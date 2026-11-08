package com.licensis.notaire.adapter.in.web.audit;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;
import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort.PageResult;
import com.licensis.notaire.dto.DtoAuditRecord;

/**
 * Wraps either a flat list or a paginated page while serializing as the legacy JSON shapes (#577).
 */
public final class AuditRecordGetAllResponse {

    private final Object body;

    private AuditRecordGetAllResponse(Object body) {
        this.body = body;
    }

    public static AuditRecordGetAllResponse flat(List<DtoAuditRecord> records) {
        return new AuditRecordGetAllResponse(records);
    }

    public static AuditRecordGetAllResponse page(PageResult page) {
        return new AuditRecordGetAllResponse(new AuditRecordPageResponse(
                page.content(),
                page.number(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last()));
    }

    @JsonValue
    public Object json() {
        return body;
    }
}
