package com.licensis.notaire.application.usecase.property;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.property.QueryPropertiesUseCase;
import com.licensis.notaire.application.port.out.property.PropertyCatalogPort;
import com.licensis.notaire.domain.property.Property;
import com.licensis.notaire.domain.property.PropertyNotFoundException;

@Service
@Transactional(readOnly = true)
public class PropertyQueryService implements QueryPropertiesUseCase {

    private final PropertyCatalogPort catalog;

    public PropertyQueryService(PropertyCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Property> findAll() {
        return catalog.findAll();
    }

    @Override
    public Property getById(int id) {
        return catalog.findById(id).orElseThrow(() -> new PropertyNotFoundException(id));
    }
}
