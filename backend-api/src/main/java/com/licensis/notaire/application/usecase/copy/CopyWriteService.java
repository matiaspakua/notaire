package com.licensis.notaire.application.usecase.copy;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.copy.ManageCopiesUseCase;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyCommand;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult;

/**
 * Write-side use case for copies — delegates to {@link CopyOpsPort} (issue #1779).
 */
@Service
public class CopyWriteService implements ManageCopiesUseCase {

    private final CopyOpsPort ops;

    public CopyWriteService(CopyOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(CopyCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, CopyCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
