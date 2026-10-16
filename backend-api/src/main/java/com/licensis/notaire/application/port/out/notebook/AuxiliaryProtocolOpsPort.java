package com.licensis.notaire.application.port.out.notebook;

import java.util.Date;
import java.util.List;

import com.licensis.notaire.domain.folio.Folio;

/**
 * Outbound persistence/ops port for auxiliary protocol (CU81 / issue #1825).
 */
public interface AuxiliaryProtocolOpsPort {

    record DeedView(
            Integer idDeed,
            int number,
            String body,
            String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            int version) {}

    sealed interface StartResult {
        record Created(DeedView view) implements StartResult {}

        record BadRequest(String message) implements StartResult {}

        record NotFound(String message) implements StartResult {}
    }

    List<Folio> listAvailableFolios();

    StartResult startDeed(Integer idFolio, String body, Date dateDeedrecording);
}
