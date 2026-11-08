package com.licensis.notaire.application.usecase.management;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.management.QueryManagementStatusesUseCase;
import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.management.ManagementStatusUsagePort;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementStatusNotFoundException;

@Service
@Transactional(readOnly = true)
public class ManagementStatusQueryService implements QueryManagementStatusesUseCase {

    private final ManagementStatusCatalogPort catalog;
    private final ManagementStatusUsagePort usage;

    public ManagementStatusQueryService(ManagementStatusCatalogPort catalog, ManagementStatusUsagePort usage) {
        this.catalog = catalog;
        this.usage = usage;
    }

    @Override
    public List<ManagementStatus> findAll() {
        return catalog.findAll();
    }

    @Override
    public List<ManagementStatus> searchByName(String text) {
        return catalog.findByNameContaining(text);
    }

    @Override
    public ManagementStatus getById(int id) {
        return catalog.findById(id).orElseThrow(() -> new ManagementStatusNotFoundException(id));
    }

    @Override
    public boolean isInUse(int id) {
        getById(id);
        return usage.isUsedByDeedManagements(id);
    }
}
