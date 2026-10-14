package com.licensis.notaire.application.port.in.folio;

import java.util.List;

import com.licensis.notaire.domain.folio.FolioType;

public interface QueryFolioTypesUseCase {

    List<FolioType> findAll();

    List<FolioType> searchByName(String text);

    /** @throws com.licensis.notaire.domain.folio.FolioTypeNotFoundException when absent */
    FolioType getById(int id);

    /** @throws com.licensis.notaire.domain.folio.FolioTypeNotFoundException when absent */
    boolean isInUse(int id);
}
