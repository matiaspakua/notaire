package com.licensis.notaire.application.port.out.notebook;

import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Notebook;

import java.util.List;

/**
 * Outbound port for folio operations within notebook context.
 */
public interface NotebookFolioOperationPort {

    List<Folio> findAllById(List<Integer> ids);

    void saveAllFolios(List<Folio> folios);
}
