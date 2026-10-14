package com.licensis.notaire.application.usecase.property;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.property.ManagePropertiesUseCase;
import com.licensis.notaire.application.port.out.property.PropertyCatalogPort;
import com.licensis.notaire.domain.property.Property;
import com.licensis.notaire.domain.property.PropertyDetails;
import com.licensis.notaire.domain.property.PropertyNotFoundException;

@Service
@Transactional
public class PropertyManagementService implements ManagePropertiesUseCase {

    private final PropertyCatalogPort catalog;

    public PropertyManagementService(PropertyCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public Property create(PropertyDetails details) {
        return catalog.save(Property.create(details));
    }

    @Override
    public void update(int id, PropertyDetails details) {
        Property existing = catalog.findById(id).orElseThrow(() -> new PropertyNotFoundException(id));
        catalog.save(existing.replaceDetails(details));
    }

    @Override
    public void delete(int id) {
        if (!catalog.deleteById(id)) {
            throw new PropertyNotFoundException(id);
        }
    }
}
