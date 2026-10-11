package com.licensis.notaire.application.usecase.document;

import com.licensis.notaire.application.port.out.document.ExternalDocumentRepositoryPort;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.licensis.notaire.application.usecase.management.ManagementTransitionService;

import java.util.List;

/**
 * CU10 — Record movements of external-entity documentation. Exposes the
 * documents a management must present to an external entity (registry,
 * cadastre) and records each movement. When every such document is delivered,
 * it tries an automatic transition to the stored status "Documentacion Completa".
 */
@Service
public class ExternalEntityDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ExternalEntityDocumentService.class);

    private final ExternalDocumentRepositoryPort documentRepository;
    private final ManagementTransitionService managementTransitionService;

    public ExternalEntityDocumentService(ExternalDocumentRepositoryPort documentRepository,
            ManagementTransitionService managementTransitionService) {
        this.documentRepository = documentRepository;
        this.managementTransitionService = managementTransitionService;
    }

    @Transactional(readOnly = true)
    public DtoManagementExternalEntityDocuments getDocuments(Integer idManagement) {
        DeedManagement management = findManagementOrThrow(idManagement);
        List<SubmittedDocument> documents = documentRepository
                .findByManagementAndDeliveredBy(idManagement,
                        BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA);
        return toDto(management, documents);
    }

    @Transactional
    public DtoExternalEntityDocument registerMovement(Integer idManagement, Integer idSubmittedDocument,
            DtoExternalEntityDocumentMovement movement) {
        findManagementOrThrow(idManagement);
        SubmittedDocument document = documentRepository.findSubmittedDocumentById(idSubmittedDocument)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submitted document not found with ID: " + idSubmittedDocument));
        validateBelongsToManagement(document, idManagement);
        validateIsExternalEntity(document);

        applyMovement(document, movement);
        SubmittedDocument guardado = documentRepository.saveSubmittedDocument(document);

        return toDto(guardado);
    }

    /**
     * Tries to move the management to the stored status "Documentacion Completa"
     * when every external-entity document has been delivered. Called as its own
     * step after {@link #registerMovement}, in a separate top-level transaction:
     * a transition the workflow does not define is a best-effort side effect and
     * must not invalidate the movement already saved. Only a call outside the
     * {@code registerMovement} transaction keeps Spring from marking that
     * transaction rollback-only when {@link ManagementTransitionService#transition}
     * fails.
     */
    public void tryCompleteDocumentation(Integer idManagement) {
        List<SubmittedDocument> documents = documentRepository
                .findByManagementAndDeliveredBy(idManagement,
                        BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA);
        boolean todosEntregados = !documents.isEmpty()
                && documents.stream().allMatch(doc -> Boolean.TRUE.equals(doc.getDelivered()));
        if (!todosEntregados) {
            return;
        }
        try {
            managementTransitionService.transition(idManagement, BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA);
        } catch (BusinessValidationException | ResourceNotFoundException e) {
            log.warn("Could not automatically transition management {} to '{}': {}", idManagement,
                    BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA, e.getMessage());
        }
    }

    private DeedManagement findManagementOrThrow(Integer idManagement) {
        return documentRepository.findManagementById(idManagement)
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
        if (!BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA.equals(document.getDeliveredBy())) {
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
        return documentRepository.findProceduresByManagementId(idManagement).stream()
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
