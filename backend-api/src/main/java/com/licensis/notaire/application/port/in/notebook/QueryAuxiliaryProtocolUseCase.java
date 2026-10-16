package com.licensis.notaire.application.port.in.notebook;

import java.util.List;

import com.licensis.notaire.domain.folio.Folio;

/**
 * Read-side inbound port for auxiliary protocol (CU81 / issue #1825).
 */
public interface QueryAuxiliaryProtocolUseCase {

    List<Folio> listAvailableFolios();
}
