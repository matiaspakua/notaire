package com.licensis.notaire.application.usecase.notebook;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.notebook.QueryNotebooksUseCase;
import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort;
import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort.NotebookView;

/**
 * Read-side use case for notebooks — delegates to {@link NotebookOpsPort} (issue #1817).
 */
@Service
public class NotebookQueryUseCaseService implements QueryNotebooksUseCase {

    private final NotebookOpsPort ops;

    public NotebookQueryUseCaseService(NotebookOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotebookView> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotebookView> findById(Integer id) {
        return ops.findById(id);
    }
}
