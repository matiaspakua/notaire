package com.licensis.notaire.application.usecase.folio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.folio.QueryFoliosUseCase;
import com.licensis.notaire.application.port.out.folio.FolioCatalogPort;
import com.licensis.notaire.domain.folio.Folio;

@Service
@Transactional(readOnly = true)
public class FolioQueryService implements QueryFoliosUseCase {

    private final FolioCatalogPort catalog;

    public FolioQueryService(FolioCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Folio> findAll() {
        return catalog.findAll();
    }

    @Override
    public List<Folio> findByStatus(String status) {
        return catalog.findByStatus(status);
    }

    @Override
    public Optional<Folio> findById(int id) {
        return catalog.findById(id);
    }

    @Override
    public Optional<Boolean> isInUse(int id) {
        return catalog.findById(id).map(Folio::isInUse);
    }
}
