package com.licensis.notaire.application.port.out.folio;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.folio.FolioType;

/** Folio type storage in domain terms. */
public interface FolioTypeCatalogPort {

    List<FolioType> findAll();

    List<FolioType> findByNameContaining(String text);

    Optional<FolioType> findById(int id);

    /** Inserts when the type has no id, otherwise updates it. */
    FolioType save(FolioType type);

    /**
     * @return false when no type had that id
     * @throws com.licensis.notaire.domain.folio.FolioTypeReferencedException when other records still reference it
     */
    boolean deleteById(int id);
}
