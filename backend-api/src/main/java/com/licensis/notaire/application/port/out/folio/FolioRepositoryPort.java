package com.licensis.notaire.application.port.out.folio;

import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for folio persistence operations.
 */
public interface FolioRepositoryPort {

    Folio save(Folio entity);

    Optional<Folio> findById(Integer id);

    List<Folio> findAll();

    List<Folio> findAvailableAuxiliaryFolios();

    Optional<Integer> findMaxNumberDeedAuxiliary();

    List<Folio> findAllByIdIn(List<Integer> ids);

    Optional<FolioType> findFolioTypeById(Integer id);

    boolean existsById(Integer id);

    void deleteById(Integer id);
}
