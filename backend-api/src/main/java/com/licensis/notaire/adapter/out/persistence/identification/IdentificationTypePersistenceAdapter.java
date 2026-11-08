package com.licensis.notaire.adapter.out.persistence.identification;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeUsagePort;
import com.licensis.notaire.domain.identification.IdentificationType;
import com.licensis.notaire.domain.identification.IdentificationTypeInUseException;
import com.licensis.notaire.domain.identification.IdentificationTypeNotFoundException;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.PersonRepository;

/**
 * Maps the legacy {@code business.IdentificationType} JPA entity, which stays because Person, Procedure and
 * DeedManagement still reference it, to the pure domain type (issue #1700).
 */
@Component
public class IdentificationTypePersistenceAdapter implements IdentificationTypeCatalogPort, IdentificationTypeUsagePort {

    private final IdentificationTypeRepository types;
    private final PersonRepository people;

    public IdentificationTypePersistenceAdapter(IdentificationTypeRepository types, PersonRepository people) {
        this.types = types;
        this.people = people;
    }

    @Override
    public List<IdentificationType> findAll() {
        return types.findAll().stream().map(IdentificationTypePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<IdentificationType> findById(int id) {
        return types.findById(id).map(IdentificationTypePersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsById(int id) {
        return types.existsById(id);
    }

    @Override
    public IdentificationType save(IdentificationType type) {
        com.licensis.notaire.business.IdentificationType entity = type.id() == null
                ? new com.licensis.notaire.business.IdentificationType()
                : types.findById(type.id()).orElseThrow(() -> new IdentificationTypeNotFoundException(type.id()));
        entity.setName(type.name());
        entity.setCharacters(type.characters());
        return toDomain(types.save(entity));
    }

    @Override
    public void deleteById(int id) {
        try {
            types.deleteById(id);
            types.flush();
        } catch (DataIntegrityViolationException referenced) {
            throw new IdentificationTypeInUseException(referenced);
        }
    }

    @Override
    public boolean isUsedByPeople(int id) {
        return people.existsByFkIdIdentificationTypeIdIdentificationType(id);
    }

    private static IdentificationType toDomain(com.licensis.notaire.business.IdentificationType entity) {
        return IdentificationType.restore(entity.getIdIdentificationType(), entity.getName(), entity.getCharacters(),
                entity.getVersion());
    }
}
