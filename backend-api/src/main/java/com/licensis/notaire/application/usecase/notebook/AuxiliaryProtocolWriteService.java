package com.licensis.notaire.application.usecase.notebook;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.notebook.ManageAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.StartResult;

/**
 * Write-side use case for auxiliary protocol — delegates to {@link AuxiliaryProtocolOpsPort}
 * (issue #1825).
 */
@Service
public class AuxiliaryProtocolWriteService implements ManageAuxiliaryProtocolUseCase {

    private final AuxiliaryProtocolOpsPort ops;

    public AuxiliaryProtocolWriteService(AuxiliaryProtocolOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public StartResult startDeed(Integer idFolio, String body, Date dateDeedrecording) {
        return ops.startDeed(idFolio, body, dateDeedrecording);
    }
}
