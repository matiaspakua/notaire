package com.licensis.notaire.adapter.out.persistence.folio;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeUsagePort;
import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeNotFoundException;
import com.licensis.notaire.domain.folio.FolioTypeReferencedException;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;

/**
 * Maps the legacy {@code business.FolioType} JPA entity, which stays because folios, the deed and notebook services
 * and the reports still use it, to the pure domain type (issue #1703).
 */
@Component
public class FolioTypePersistenceAdapter implements FolioTypeCatalogPort, FolioTypeUsagePort {

    private final FolioTypeRepository types;
    private final FolioRepository folios;

    public FolioTypePersistenceAdapter(FolioTypeRepository types, FolioRepository folios) {
        this.types = types;
        this.folios = folios;
    }

    @Override
    public List<FolioType> findAll() {
        return types.findAll().stream().map(FolioTypePersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<FolioType> findByNameContaining(String text) {
        return types.findByNameContaining(text).stream().map(FolioTypePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<FolioType> findById(int id) {
        return types.findById(id).map(FolioTypePersistenceAdapter::toDomain);
    }

    @Override
    public FolioType save(FolioType type) {
        com.licensis.notaire.business.FolioType entity = type.id() == null
                ? new com.licensis.notaire.business.FolioType()
                : types.findById(type.id()).orElseThrow(() -> new FolioTypeNotFoundException(type.id()));
        entity.setName(type.name());
        entity.setNotes(type.notes());
        entity.setEnabled(type.enabled());
        entity.setIsAuxiliary(type.auxiliary());
        return toDomain(types.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!types.existsById(id)) {
            return false;
        }
        try {
            types.deleteById(id);
            types.flush();
        } catch (DataIntegrityViolationException referenced) {
            throw new FolioTypeReferencedException(referenced);
        }
        return true;
    }

    @Override
    public boolean isUsedByFolios(int id) {
        return !folios.findByFkIdFolioTypeIdFolioType(id).isEmpty();
    }

    private static FolioType toDomain(com.licensis.notaire.business.FolioType entity) {
        return FolioType.restore(entity.getIdFolioType(), entity.getName(), entity.getNotes(), entity.getEnabled(),
                entity.isIsAuxiliary(), entity.getVersion());
    }
}
