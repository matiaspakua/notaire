package com.licensis.notaire.application.port.in.deed;

import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedCommand;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult;

/**
 * Write-side inbound port for deeds (issue #1811).
 */
public interface ManageDeedsUseCase {

    SaveResult create(DeedCommand command);

    SaveResult update(Integer id, DeedCommand command);

    DeleteResult delete(Integer id);

    DeedView sign(Integer id);
}
