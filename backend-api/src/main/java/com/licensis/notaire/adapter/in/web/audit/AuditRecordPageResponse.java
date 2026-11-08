package com.licensis.notaire.adapter.in.web.audit;

import java.util.List;

import com.licensis.notaire.dto.DtoAuditRecord;

/** Paginated audit-log body — same JSON keys clients already consume (#577). */
public record AuditRecordPageResponse(
        List<DtoAuditRecord> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
}
