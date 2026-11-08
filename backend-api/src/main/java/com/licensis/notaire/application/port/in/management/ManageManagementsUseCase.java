package com.licensis.notaire.application.port.in.management;

import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ArchiveResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseCommand;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementCommand;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult;

/**
 * Write-side inbound port for deed managements (issue #1772).
 */
public interface ManageManagementsUseCase {

    CompleteCaseResult createCompleteCase(CompleteCaseCommand command);

    CompleteCaseResult updateCompleteCase(Integer id, CompleteCaseCommand command);

    SaveResult create(ManagementCommand command);

    SaveResult update(Integer id, ManagementCommand command);

    DeleteResult delete(Integer id);

    ArchiveResult archive(Integer id, boolean confirmed);
}
