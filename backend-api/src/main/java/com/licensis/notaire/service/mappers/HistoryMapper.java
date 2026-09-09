package com.licensis.notaire.service.mappers;

import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.business.History;

public final class HistoryMapper {

    private HistoryMapper() {}

    public static DtoHistorySummary toDto(History h) {
        return new DtoHistorySummary(
                h.getIdHistory(),
                h.getDate(),
                h.getNotes(),
                h.getFkIdManagement() != null ? h.getFkIdManagement().getIdManagement() : null,
                h.getFkIdManagementStatus() != null ? h.getFkIdManagementStatus().getIdManagementStatus() : null,
                h.getFkIdManagementStatus() != null ? h.getFkIdManagementStatus().getName() : null
        );
    }
}
