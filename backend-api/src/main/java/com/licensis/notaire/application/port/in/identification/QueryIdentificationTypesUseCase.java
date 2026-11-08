package com.licensis.notaire.application.port.in.identification;

import java.util.List;

import com.licensis.notaire.domain.identification.IdentificationType;

public interface QueryIdentificationTypesUseCase {

    List<IdentificationType> findAll();

    /** @throws com.licensis.notaire.domain.identification.IdentificationTypeNotFoundException when absent */
    IdentificationType getById(int id);
}
