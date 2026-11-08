package com.licensis.notaire.adapter.out.persistence.notebook;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.domain.folio.Folio;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;

/**
 * Persistence/ops adapter for auxiliary protocol. Holds entity mapping previously on
 * {@code AuxiliaryProtocolController} (issue #1825).
 */
@Component
public class AuxiliaryProtocolOpsAdapter implements AuxiliaryProtocolOpsPort {

    private final AuxiliaryProtocolService auxiliaryProtocolService;

    public AuxiliaryProtocolOpsAdapter(AuxiliaryProtocolService auxiliaryProtocolService) {
        this.auxiliaryProtocolService = auxiliaryProtocolService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Folio> listAvailableFolios() {
        return auxiliaryProtocolService.listAvailableFolios();
    }

    @Override
    public StartResult startDeed(Integer idFolio, String body, Date dateDeedrecording) {
        // No outer @Transactional: catching BusinessValidationException into BadRequest must not
        // mark the Spring TX rollback-only (UnexpectedRollbackException → 500 instead of 400).
        try {
            Deed created = auxiliaryProtocolService.startDeed(idFolio, body, dateDeedrecording);
            return new StartResult.Created(toView(created));
        } catch (BusinessValidationException e) {
            return new StartResult.BadRequest(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return new StartResult.NotFound(e.getMessage());
        }
    }

    private static DeedView toView(Deed deed) {
        return new DeedView(
                deed.getIdDeed(),
                deed.getNumber(),
                deed.getBody(),
                deed.getStatus(),
                deed.getDateDeedrecording(),
                deed.getDateRegistration(),
                deed.getRegistrationEntryNumber(),
                deed.getNotes(),
                deed.getVersion());
    }
}
