package com.licensis.notaire.application.port.in.folio;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.folio.Folio;

public interface QueryFoliosUseCase {

    List<Folio> findAll();

    List<Folio> findByStatus(String status);

    Optional<Folio> findById(int id);

    /** @return empty when the folio does not exist */
    Optional<Boolean> isInUse(int id);
}
