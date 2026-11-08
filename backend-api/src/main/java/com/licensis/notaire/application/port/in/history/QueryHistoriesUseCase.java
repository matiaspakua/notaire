package com.licensis.notaire.application.port.in.history;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.history.HistoryOpsPort.PageResult;
import com.licensis.notaire.dto.DtoHistorySummary;

/**
 * Read-side inbound port for management history (issue #1775).
 */
public interface QueryHistoriesUseCase {

    PageResult findAll(int page, int size, String sort);

    Optional<DtoHistorySummary> findById(Integer id);

    List<DtoHistorySummary> findByManagementId(Integer idManagement);
}
