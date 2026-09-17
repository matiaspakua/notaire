package com.licensis.notaire.adapter.out.persistence.property;

import com.licensis.notaire.application.port.out.property.PropertyRepositoryPort;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.repository.PropertyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the property slice.
 * The only place where {@link PropertyRepository} is touched for this slice.
 */
@Component
public class PropertyPersistenceAdapter implements PropertyRepositoryPort {

    private final PropertyRepository propertyRepository;

    public PropertyPersistenceAdapter(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    @Override
    public Optional<Property> findById(Integer id) {
        return propertyRepository.findById(id);
    }

    @Override
    public Property save(Property entity) {
        return propertyRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        propertyRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return propertyRepository.existsById(id);
    }
}
