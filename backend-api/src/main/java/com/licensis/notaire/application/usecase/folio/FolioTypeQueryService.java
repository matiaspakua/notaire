package com.licensis.notaire.application.usecase.folio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.folio.QueryFolioTypesUseCase;
import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeUsagePort;
import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeNotFoundException;

@Service
@Transactional(readOnly = true)
public class FolioTypeQueryService implements QueryFolioTypesUseCase {

    private final FolioTypeCatalogPort catalog;
    private final FolioTypeUsagePort usage;

    public FolioTypeQueryService(FolioTypeCatalogPort catalog, FolioTypeUsagePort usage) {
        this.catalog = catalog;
        this.usage = usage;
    }

    @Override
    public List<FolioType> findAll() {
        return catalog.findAll();
    }

    @Override
    public List<FolioType> searchByName(String text) {
        return catalog.findByNameContaining(text);
    }

    @Override
    public FolioType getById(int id) {
        return catalog.findById(id).orElseThrow(() -> new FolioTypeNotFoundException(id));
    }

    @Override
    public boolean isInUse(int id) {
        getById(id);
        return usage.isUsedByFolios(id);
    }
}
