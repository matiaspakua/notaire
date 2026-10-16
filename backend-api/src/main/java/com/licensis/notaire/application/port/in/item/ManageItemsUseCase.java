package com.licensis.notaire.application.port.in.item;

import com.licensis.notaire.application.port.out.item.ItemOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemCommand;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.SaveResult;

/**
 * Write-side inbound port for budget items (issue #1809).
 */
public interface ManageItemsUseCase {

    SaveResult create(ItemCommand command);

    SaveResult update(Integer id, ItemCommand command);

    DeleteResult delete(Integer id);
}
