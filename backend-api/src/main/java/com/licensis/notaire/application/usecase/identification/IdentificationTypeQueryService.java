package com.licensis.notaire.application.usecase.identification;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.identification.QueryIdentificationTypesUseCase;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;
import com.licensis.notaire.domain.identification.IdentificationType;
import com.licensis.notaire.domain.identification.IdentificationTypeNotFoundException;

@Service
@Transactional(readOnly = true)
public class IdentificationTypeQueryService implements QueryIdentificationTypesUseCase {

    static final String CACHE = "identification-types";

    private final IdentificationTypeCatalogPort catalog;

    public IdentificationTypeQueryService(IdentificationTypeCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    @Cacheable(CACHE)
    public List<IdentificationType> findAll() {
        return catalog.findAll();
    }

    @Override
    public IdentificationType getById(int id) {
        return catalog.findById(id).orElseThrow(() -> new IdentificationTypeNotFoundException(id));
    }
}
