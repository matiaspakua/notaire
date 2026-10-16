package com.licensis.notaire.application.usecase.management;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseView;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementView;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.PageResult;
import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.dto.DtoManagementSummary;

/**
 * Read-side use case for deed managements — delegates to {@link ManagementOpsPort} (issue #1772).
 */
@Service
public class ManagementQueryUseCaseService implements QueryManagementsUseCase {

    private final ManagementOpsPort ops;

    public ManagementQueryUseCaseService(ManagementOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        return ops.findAll(page, size, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoManagementSummary> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoManagementSummary> findByNumber(int number) {
        return ops.findByNumber(number);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagementView> findByClientPersonId(Integer idPerson) {
        return ops.findByClientPersonId(idPerson);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DtoHistorySummary> findCurrentStatus(Integer id) {
        return ops.findCurrentStatus(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompleteCaseView> findCompleteCase(Integer id) {
        return ops.findCompleteCase(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<DtoHistorySummary>> findHistory(Integer id) {
        return ops.findHistory(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findCurrentStatusName(Integer id) {
        return ops.findCurrentStatusName(id);
    }
}
