package com.licensis.notaire.application.usecase.management;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ArchiveResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseCommand;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementCommand;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult;

/**
 * Write-side use case for deed managements — delegates to {@link ManagementOpsPort} (issue #1772).
 * Transactions live on the outbound adapter so caught domain/not-found paths do not mark an outer
 * TX rollback-only (same UnexpectedRollbackException pattern as #1745).
 */
@Service
public class ManagementWriteService implements ManageManagementsUseCase {

    private final ManagementOpsPort ops;

    public ManagementWriteService(ManagementOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public CompleteCaseResult createCompleteCase(CompleteCaseCommand command) {
        return ops.createCompleteCase(command);
    }

    @Override
    public CompleteCaseResult updateCompleteCase(Integer id, CompleteCaseCommand command) {
        return ops.updateCompleteCase(id, command);
    }

    @Override
    public SaveResult create(ManagementCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, ManagementCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }

    @Override
    public ArchiveResult archive(Integer id, boolean confirmed) {
        return ops.archive(id, confirmed);
    }
}
