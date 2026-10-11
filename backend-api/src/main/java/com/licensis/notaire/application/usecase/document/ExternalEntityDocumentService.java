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
 * CU10 - Record movimientos of documentation of entidades externas: expone
 * the documentation of a management that must be presented by entidades
 * externas (registros, catastro) and permite record the datos of its
 * movimiento, disparando the transition automática of the management a
 * "Documentation Completa" when all esos documents quedan entregados.
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
                        "Documento presentado not found with ID: " + idSubmittedDocument));
        validateBelongsToManagement(document, idManagement);
        validateIsExternalEntity(document);

        applyMovement(document, movement);
        SubmittedDocument guardado = documentRepository.saveSubmittedDocument(document);

        return toDto(guardado);
    }

    /**
     * Intenta transition the management a "Documentation Completa" when all their
     * documentos de entidad externa quedaron entregados. Se invoca como un paso
     * independiente después of {@link #registrarMovimiento}, in its propia
     * transacción of nivel superior: a transition not definida in the workflow is
     * un efecto colateral "best effort" que nunca debe invalidar el movimiento ya
     * guardado, and only a llamada fuera of the transacción of {@code
     * registrarMovimiento} evita that Spring marque that transacción as
     * rollback-only cuando {@link GestionTransitionService#transition} falla.
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
            log.warn("Not pudo transition automáticamente the management {} a '{}': {}", idManagement,
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
                    "El documento " + document.getIdSubmittedDocument() + " not belongs to the management "
                            + idManagement);
        }
    }

    private static void validateIsExternalEntity(SubmittedDocument document) {
        if (!BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA.equals(document.getDeliveredBy())) {
            throw new BusinessValidationException(
                    "El documento " + document.getIdSubmittedDocument() + " no es de entidad externa");
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
