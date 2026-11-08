package com.licensis.notaire.application.usecase.history;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.history.QueryHistoriesUseCase;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.PageResult;
import com.licensis.notaire.dto.DtoHistorySummary;

/**
 * Read-side use case for management history — delegates to {@link HistoryOpsPort} (issue #1775).
 */
@Service
public class HistoryQueryUseCaseService implements QueryHistoriesUseCase {

    private final HistoryOpsPort ops;

    public HistoryQueryUseCaseService(HistoryOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        return ops.findAll(page, size, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoHistorySummary> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoHistorySummary> findByManagementId(Integer idManagement) {
        return ops.findByManagementId(idManagement);
    }
}
