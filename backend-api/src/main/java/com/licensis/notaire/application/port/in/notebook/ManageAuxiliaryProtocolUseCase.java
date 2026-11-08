package com.licensis.notaire.application.port.in.notebook;

import java.util.Date;

import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.StartResult;

/**
 * Write-side inbound port for auxiliary protocol (CU81 / issue #1825).
 */
public interface ManageAuxiliaryProtocolUseCase {

    StartResult startDeed(Integer idFolio, String body, Date dateDeedrecording);
}
