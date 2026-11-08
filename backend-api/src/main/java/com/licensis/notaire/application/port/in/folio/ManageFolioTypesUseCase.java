package com.licensis.notaire.application.port.in.folio;

import com.licensis.notaire.domain.folio.FolioType;

public interface ManageFolioTypesUseCase {

    /** {@code version} is the one the client read; creating ignores it, updating must match the stored one. */
    record SaveFolioTypeCommand(String name, String notes, boolean enabled, boolean auxiliary, int version) {}

    FolioType create(SaveFolioTypeCommand command);

    void update(int id, SaveFolioTypeCommand command);

    void delete(int id);
}
