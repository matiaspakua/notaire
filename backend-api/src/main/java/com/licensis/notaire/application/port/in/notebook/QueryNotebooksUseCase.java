package com.licensis.notaire.application.port.in.notebook;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort.NotebookView;

/**
 * Read-side inbound port for notebooks (issue #1817).
 */
public interface QueryNotebooksUseCase {

    List<NotebookView> findAll();

    Optional<NotebookView> findById(Integer id);
}
