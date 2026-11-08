package com.licensis.notaire.application.port.in.identification;

import com.licensis.notaire.domain.identification.IdentificationType;

public interface ManageIdentificationTypesUseCase {

    record SaveIdentificationTypeCommand(String name, String characters) {}

    IdentificationType create(SaveIdentificationTypeCommand command);

    void update(int id, SaveIdentificationTypeCommand command);

    void delete(int id);
}
