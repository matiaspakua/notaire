package com.licensis.notaire.adapter.out.persistence.property;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.property.PropertyCatalogPort;
import com.licensis.notaire.domain.property.Property;
import com.licensis.notaire.domain.property.PropertyDetails;
import com.licensis.notaire.domain.property.PropertyNotFoundException;
import com.licensis.notaire.repository.PropertyRepository;

/**
 * Maps the legacy {@code business.Property} JPA entity, which stays because procedures, reports and registration
 * drafts still use it, to the pure domain type (issue #1702).
 */
@Component
public class PropertyCatalogAdapter implements PropertyCatalogPort {

    private final PropertyRepository properties;

    public PropertyCatalogAdapter(PropertyRepository properties) {
        this.properties = properties;
    }

    @Override
    public List<Property> findAll() {
        return properties.findAll().stream().map(PropertyCatalogAdapter::toDomain).toList();
    }

    @Override
    public Optional<Property> findById(int id) {
        return properties.findById(id).map(PropertyCatalogAdapter::toDomain);
    }

    @Override
    public Property save(Property property) {
        com.licensis.notaire.business.Property entity = property.id() == null
                ? new com.licensis.notaire.business.Property()
                : properties.findById(property.id()).orElseThrow(() -> new PropertyNotFoundException(property.id()));
        PropertyDetails details = property.details();
        entity.setCadastralDesignation(details.cadastralDesignation());
        entity.setFiscalAppraisal(details.fiscalAppraisal());
        entity.setAddress(details.address());
        entity.setNotes(details.notes());
        entity.setRegistrationNumber(details.registrationNumber());
        entity.setVolumeFolioLandRecord(details.volumeFolioLandRecord());
        entity.setBoundaries(details.boundaries());
        return toDomain(properties.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!properties.existsById(id)) {
            return false;
        }
        properties.deleteById(id);
        return true;
    }

    private static Property toDomain(com.licensis.notaire.business.Property entity) {
        return Property.restore(entity.getIdProperty(),
                new PropertyDetails(entity.getCadastralDesignation(), entity.getFiscalAppraisal(), entity.getAddress(),
                        entity.getNotes(), entity.getRegistrationNumber(), entity.getVolumeFolioLandRecord(),
                        entity.getBoundaries()),
                entity.getVersion());
    }
}
