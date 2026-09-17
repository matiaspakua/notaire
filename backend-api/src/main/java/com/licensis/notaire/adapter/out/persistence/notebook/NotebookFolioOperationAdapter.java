package com.licensis.notaire.adapter.out.persistence.notebook;

import com.licensis.notaire.application.port.out.notebook.NotebookFolioOperationPort;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Persistence adapter for folio operations within notebook context.
 */
@Component
public class NotebookFolioOperationAdapter implements NotebookFolioOperationPort {

    private final FolioRepository folioRepository;

    public NotebookFolioOperationAdapter(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    @Override
    public List<Folio> findAllById(List<Integer> ids) {
        return folioRepository.findAllByIdFolioIn(ids);
    }

    @Override
    public void saveAllFolios(List<Folio> folios) {
        folioRepository.saveAll(folios);
    }
}
