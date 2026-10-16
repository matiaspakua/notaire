package com.licensis.notaire.application.port.in.management;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseView;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementView;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.PageResult;
import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.dto.DtoManagementSummary;

/**
 * Read-side inbound port for deed managements (issue #1772).
 */
public interface QueryManagementsUseCase {

    PageResult findAll(int page, int size, String sort);

    Optional<DtoManagementSummary> findById(Integer id);

    Optional<DtoManagementSummary> findByNumber(int number);

    List<ManagementView> findByClientPersonId(Integer idPerson);

    Optional<DtoHistorySummary> findCurrentStatus(Integer id);

    Optional<CompleteCaseView> findCompleteCase(Integer id);

    Optional<List<DtoHistorySummary>> findHistory(Integer id);

    /** Lightweight status name for transition HTTP mapping. */
    Optional<String> findCurrentStatusName(Integer id);
}
