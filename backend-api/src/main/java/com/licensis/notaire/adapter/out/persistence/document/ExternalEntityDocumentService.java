package com.licensis.notaire.adapter.out.persistence.document;

import com.licensis.notaire.application.port.in.document.ExternalEntityDocumentUseCase;
import com.licensis.notaire.dto.DtoExternalEntityDocument;
import com.licensis.notaire.dto.DtoManagementExternalEntityDocuments;
import com.licensis.notaire.dto.DtoExternalEntityDocumentMovement;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.licensis.notaire.application.port.out.management.ManagementAutoTransitionPort;

import java.util.List;

/**
 * CU10 — Record movements of external-entity documentation. Outbound adapter
 * implementing the inbound port (issue #1866).
 */
@Service
public class ExternalEntityDocumentService implements ExternalEntityDocumentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExternalEntityDocumentService.class);

    private final SubmittedDocumentRepository submittedDocumentRepository;
    private final DeedManagementRepository managementRepository;
    private final ProcedureRepository procedureRepository;
    private final ManagementAutoTransitionPort managementAutoTransition;

    public ExternalEntityDocumentService(
            SubmittedDocumentRepository submittedDocumentRepository,
            DeedManagementRepository managementRepository,
            ProcedureRepository procedureRepository,
            ManagementAutoTransitionPort managementAutoTransition) {
        this.submittedDocumentRepository = submittedDocumentRepository;
        this.managementRepository = managementRepository;
        this.procedureRepository = procedureRepository;
        this.managementAutoTransition = managementAutoTransition;
    }

    @Override
    @Transactional(readOnly = true)
    public DtoManagementExternalEntityDocuments getDocuments(Integer idManagement) {
        DeedManagement management = findManagementOrThrow(idManagement);
        List<SubmittedDocument> documents = submittedDocumentRepository
                .findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
                        idManagement, BusinessConstants.DocumentationExternalEntity);
        return toDto(management, documents);
    }

    @Override
    @Transactional
    public DtoExternalEntityDocument registerMovement(Integer idManagement, Integer idSubmittedDocument,
            DtoExternalEntityDocumentMovement movement) {
        findManagementOrThrow(idManagement);
        SubmittedDocument document = submittedDocumentRepository.findById(idSubmittedDocument)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submitted document not found with ID: " + idSubmittedDocument));
        validateBelongsToManagement(document, idManagement);
        validateIsExternalEntity(document);

        applyMovement(document, movement);
        SubmittedDocument saved = submittedDocumentRepository.save(document);

        return toDto(saved);
    }

    /**
     * Tries to move the management to the stored status "Documentacion Completa"
     * when every external-entity document has been delivered. Called as its own
     * step after {@link #registerMovement}, in a separate top-level transaction.
     */
    @Override
    public void tryCompleteDocumentation(Integer idManagement) {
        List<SubmittedDocument> documents = submittedDocumentRepository
                .findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
                        idManagement, BusinessConstants.DocumentationExternalEntity);
        boolean allDelivered = !documents.isEmpty()
                && documents.stream().allMatch(doc -> Boolean.TRUE.equals(doc.getDelivered()));
        if (!allDelivered) {
            return;
        }
        try {
            managementAutoTransition.transition(idManagement, BusinessConstants.ManagementWithCompleteDocumentation);
        } catch (BusinessValidationException | ResourceNotFoundException e) {
            log.warn("Could not automatically transition management {} to '{}': {}", idManagement,
                    BusinessConstants.ManagementWithCompleteDocumentation, e.getMessage());
        }
    }

    private DeedManagement findManagementOrThrow(Integer idManagement) {
        return managementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException("Management not found with ID: " + idManagement));
    }

    private static void validateBelongsToManagement(SubmittedDocument document, Integer idManagement) {
        Procedure procedure = document.getFkIdProcedure();
        DeedManagement management = procedure != null ? procedure.getFkIdManagement() : null;
        if (management == null || !idManagement.equals(management.getIdManagement())) {
            throw new BusinessValidationException(
                    "Document " + document.getIdSubmittedDocument() + " does not belong to management "
                            + idManagement);
        }
    }

    private static void validateIsExternalEntity(SubmittedDocument document) {
        if (!BusinessConstants.DocumentationExternalEntity.equals(document.getDeliveredBy())) {
            throw new BusinessValidationException(
                    "Document " + document.getIdSubmittedDocument() + " is not an external-entity document");
        }
    }

    private static void applyMovement(SubmittedDocument document,
            DtoExternalEntityDocumentMovement movement) {
        if (movement.prepared() != null) {
            document.setPrepared(movement.prepared());
        }
        document.setCardNumber(movement.cardNumber());
        document.setDateEntry(movement.dateEntry());
        document.setDateExit(movement.dateExit());
        document.setFlagged(movement.flagged());
        document.setAmountToPay(movement.amountToPay());
        document.setDatePayment(movement.datePayment());
        document.setDateReleased(movement.dateReleased());
        document.setNotes(movement.notes());
        document.setDelivered(movement.delivered());
    }

    private DtoManagementExternalEntityDocuments toDto(DeedManagement management,
            List<SubmittedDocument> documents) {
        return new DtoManagementExternalEntityDocuments(
                management.getIdManagement(),
                management.getNumber(),
                management.getHeading(),
                management.getDateStart(),
                nameNotary(management.getFkIdNotaryPerson()),
                resolveCadastralDesignation(management.getIdManagement()),
                documents.stream().map(ExternalEntityDocumentService::toDto).toList());
    }

    private static String nameNotary(Person notary) {
        if (notary == null) {
            return null;
        }
        return (notary.getFirstName() + " " + notary.getLastName()).trim();
    }

    private String resolveCadastralDesignation(Integer idManagement) {
        return procedureRepository.findByFkIdManagementIdManagement(idManagement).stream()
                .map(Procedure::getFkIdProperty)
                .filter(java.util.Objects::nonNull)
                .map(Property::getCadastralDesignation)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static DtoExternalEntityDocument toDto(SubmittedDocument document) {
        return new DtoExternalEntityDocument(
                document.getIdSubmittedDocument(),
                document.getName(),
                document.getPrepared(),
                document.getCardNumber(),
                document.getDateEntry(),
                document.getDateExit(),
                document.getFlagged(),
                document.getAmountToPay(),
                document.getDatePayment(),
                document.getDateReleased(),
                document.getNotes(),
                document.getDelivered());
    }
}
