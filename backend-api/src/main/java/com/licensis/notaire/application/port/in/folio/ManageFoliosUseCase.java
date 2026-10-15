package com.licensis.notaire.application.port.in.folio;

import com.licensis.notaire.domain.folio.Folio;

public interface ManageFoliosUseCase {

    record SaveFolioCommand(
            int number, int year, String status, String notes, Integer typeFolioId, Integer notaryId, Integer deedId) {}

    Folio create(SaveFolioCommand command);

    Folio update(int id, SaveFolioCommand command);

    void delete(int id);
}
