package com.licensis.notaire.application.usecase.item;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.item.ManageItemsUseCase;
import com.licensis.notaire.application.port.out.item.ItemOpsPort;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemCommand;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.SaveResult;

/**
 * Write-side use case for items — delegates to {@link ItemOpsPort} (issue #1809).
 */
@Service
public class ItemWriteService implements ManageItemsUseCase {

    private final ItemOpsPort ops;

    public ItemWriteService(ItemOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(ItemCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, ItemCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
