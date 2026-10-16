package com.licensis.notaire.application.usecase.history;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.history.ManageHistoriesUseCase;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.HistoryCommand;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult;

/**
 * Write-side use case for management history — delegates to {@link HistoryOpsPort} (issue #1775).
 * Transactions live on the outbound adapter so caught error paths do not mark an outer TX
 * rollback-only (same UnexpectedRollbackException pattern as #1745).
 */
@Service
public class HistoryWriteService implements ManageHistoriesUseCase {

    private final HistoryOpsPort ops;

    public HistoryWriteService(HistoryOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(HistoryCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, HistoryCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
