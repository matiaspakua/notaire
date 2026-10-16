package com.licensis.notaire.application.usecase.deed;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.deed.ManageDeedsUseCase;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedCommand;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult;

/**
 * Write-side use case for deeds — delegates to {@link DeedOpsPort} (issue #1811).
 */
@Service
public class DeedWriteService implements ManageDeedsUseCase {

    private final DeedOpsPort ops;

    public DeedWriteService(DeedOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(DeedCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, DeedCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }

    @Override
    public DeedView sign(Integer id) {
        return ops.sign(id);
    }
}
