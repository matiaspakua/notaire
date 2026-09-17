package com.licensis.notaire.application.usecase.property;

import com.licensis.notaire.application.port.in.property.PropertyUseCase;
import com.licensis.notaire.application.port.out.property.PropertyRepositoryPort;
import com.licensis.notaire.business.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyService implements PropertyUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepositoryPort propertyRepositoryPort;

    public PropertyService(PropertyRepositoryPort propertyRepositoryPort) {
        this.propertyRepositoryPort = propertyRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Property> findAll() {
        logger.debug("Finding all properties");
        return propertyRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Property> findById(Integer id) {
        logger.debug("Finding property by id: {}", id);
        return propertyRepositoryPort.findById(id);
    }

    @Override
    public Property save(Property entity) {
        logger.info("Saving property id: {}", entity.getIdProperty());
        return propertyRepositoryPort.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        logger.info("Deleting property with id: {}", id);
        propertyRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return propertyRepositoryPort.existsById(id);
    }
}
