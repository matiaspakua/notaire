package com.licensis.notaire.application.port.in.notebook;

import java.util.List;

import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort.CreateResult;

/**
 * Write-side inbound port for notebooks (issue #1817).
 */
public interface ManageNotebooksUseCase {

    CreateResult create(List<Integer> idsFolio, Integer idNotary, int year, String notes);
}
