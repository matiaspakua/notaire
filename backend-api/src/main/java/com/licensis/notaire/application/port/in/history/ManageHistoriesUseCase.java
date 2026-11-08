package com.licensis.notaire.application.port.in.history;

import com.licensis.notaire.application.port.out.history.HistoryOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.HistoryCommand;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult;

/**
 * Write-side inbound port for management history (issue #1775).
 */
public interface ManageHistoriesUseCase {

    SaveResult create(HistoryCommand command);

    SaveResult update(Integer id, HistoryCommand command);

    DeleteResult delete(Integer id);
}
