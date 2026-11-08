package com.licensis.notaire.application.usecase.notebook;

import java.util.List;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.notebook.QueryAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort;
import com.licensis.notaire.domain.folio.Folio;

/**
 * Read-side use case for auxiliary protocol — delegates to {@link AuxiliaryProtocolOpsPort}
 * (issue #1825).
 */
@Service
public class AuxiliaryProtocolQueryUseCaseService implements QueryAuxiliaryProtocolUseCase {

    private final AuxiliaryProtocolOpsPort ops;

    public AuxiliaryProtocolQueryUseCaseService(AuxiliaryProtocolOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public List<Folio> listAvailableFolios() {
        return ops.listAvailableFolios();
    }
}
