package com.licensis.notaire.application.port.in.copy;

import com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyCommand;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult;

/**
 * Write-side inbound port for copies (issue #1779).
 */
public interface ManageCopiesUseCase {

    SaveResult create(CopyCommand command);

    SaveResult update(Integer id, CopyCommand command);

    DeleteResult delete(Integer id);
}
