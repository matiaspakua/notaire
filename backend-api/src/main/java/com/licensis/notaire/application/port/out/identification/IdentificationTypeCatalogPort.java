package com.licensis.notaire.application.port.out.identification;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.identification.IdentificationType;

public interface IdentificationTypeCatalogPort {

    List<IdentificationType> findAll();

    Optional<IdentificationType> findById(int id);

    boolean existsById(int id);

    /** Inserts when the type has no id, otherwise updates it. */
    IdentificationType save(IdentificationType type);

    /** @throws com.licensis.notaire.domain.identification.IdentificationTypeInUseException when still referenced */
    void deleteById(int id);
}
