package com.licensis.notaire.application.port.out.folio;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.folio.Folio;

/** Folio instance storage in domain terms (issue #1727). */
public interface FolioCatalogPort {

    List<Folio> findAll();

    List<Folio> findByStatus(String status);

    Optional<Folio> findById(int id);

    /** Auxiliary-protocol folios that are still free (issue #1727 / CU81). */
    List<Folio> findAvailableAuxiliaryFolios();

    Folio save(Folio folio);

    boolean deleteById(int id);
}
