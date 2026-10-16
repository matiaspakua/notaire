package com.licensis.notaire.application.usecase.notebook;

import java.util.List;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.notebook.ManageNotebooksUseCase;
import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort;
import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort.CreateResult;

/**
 * Write-side use case for notebooks — delegates to {@link NotebookOpsPort} (issue #1817).
 */
@Service
public class NotebookWriteService implements ManageNotebooksUseCase {

    private final NotebookOpsPort ops;

    public NotebookWriteService(NotebookOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public CreateResult create(List<Integer> idsFolio, Integer idNotary, int year, String notes) {
        return ops.create(idsFolio, idNotary, year, notes);
    }
}
