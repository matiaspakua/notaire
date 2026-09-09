package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoDocumentEntidadExterna;
import com.licensis.notaire.dto.DtoManagementDocumentsEntidadesExternas;
import com.licensis.notaire.dto.DtoMovementDocumentEntidadExterna;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU10 - Registrar movimientos de documentación de entidades externas: expone
 * la documentación de una gestión que debe ser presentada por entidades
 * externas (registros, catastro) y permite registrar los datos de su
 * movimiento, disparando la transición automática de la gestión a
 * "Documentacion Completa" cuando todos esos documentos quedan entregados.
 */
@Service
public class DocumentEntidadExternaService {

    private static final Logger log = LoggerFactory.getLogger(DocumentEntidadExternaService.class);

    private final DeedManagementRepository managementRepository;
    private final ProcedureRepository procedureRepository;
    private final SubmittedDocumentRepository submittedDocumentRepository;
    private final ManagementTransitionService managementTransitionService;

    public DocumentEntidadExternaService(DeedManagementRepository managementRepository,
            ProcedureRepository procedureRepository, SubmittedDocumentRepository submittedDocumentRepository,
            ManagementTransitionService managementTransitionService) {
        this.managementRepository = managementRepository;
        this.procedureRepository = procedureRepository;
        this.submittedDocumentRepository = submittedDocumentRepository;
        this.managementTransitionService = managementTransitionService;
    }

    @Transactional(readOnly = true)
    public DtoManagementDocumentsEntidadesExternas obtenerDocuments(Integer idManagement) {
        DeedManagement management = findManagementOrThrow(idManagement);
        List<SubmittedDocument> documents = submittedDocumentRepository
                .findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(idManagement,
                        BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA);
        return toDto(management, documents);
    }

    @Transactional
    public DtoDocumentEntidadExterna registrarMovement(Integer idManagement, Integer idSubmittedDocument,
            DtoMovementDocumentEntidadExterna movement) {
        findManagementOrThrow(idManagement);
        SubmittedDocument document = submittedDocumentRepository.findById(idSubmittedDocument)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento presentado no encontrado con ID: " + idSubmittedDocument));
        validarPerteneceAManagement(document, idManagement);
        validarEsEntidadExterna(document);

        aplicarMovement(document, movement);
        SubmittedDocument guardado = submittedDocumentRepository.save(document);

        return toDto(guardado);
    }

    /**
     * Intenta transicionar la gestión a "Documentacion Completa" cuando todos sus
     * documentos de entidad externa quedaron entregados. Se invoca como un paso
     * independiente después de {@link #registrarMovimiento}, en su propia
     * transacción de nivel superior: una transición no definida en el workflow es
     * un efecto colateral "best effort" que nunca debe invalidar el movimiento ya
     * guardado, y solo una llamada fuera de la transacción de {@code
     * registrarMovimiento} evita que Spring marque esa transacción como
     * rollback-only cuando {@link GestionTransitionService#transicionar} falla.
     */
    public void intentarCompletarDocumentacion(Integer idManagement) {
        List<SubmittedDocument> documents = submittedDocumentRepository
                .findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(idManagement,
                        BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA);
        boolean todosEntregados = !documents.isEmpty()
                && documents.stream().allMatch(doc -> Boolean.TRUE.equals(doc.getDelivered()));
        if (!todosEntregados) {
            return;
        }
        try {
            managementTransitionService.transicionar(idManagement, BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA);
        } catch (BusinessValidationException | ResourceNotFoundException e) {
            log.warn("No se pudo transicionar automáticamente la gestión {} a '{}': {}", idManagement,
                    BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA, e.getMessage());
        }
    }

    private DeedManagement findManagementOrThrow(Integer idManagement) {
        return managementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idManagement));
    }

    private static void validarPerteneceAManagement(SubmittedDocument document, Integer idManagement) {
        Procedure procedure = document.getFkIdProcedure();
        DeedManagement management = procedure != null ? procedure.getFkIdManagement() : null;
        if (management == null || !idManagement.equals(management.getIdManagement())) {
            throw new BusinessValidationException(
                    "El documento " + document.getIdSubmittedDocument() + " no pertenece a la gestión "
                            + idManagement);
        }
    }

    private static void validarEsEntidadExterna(SubmittedDocument document) {
        if (!BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA.equals(document.getDeliveredBy())) {
            throw new BusinessValidationException(
                    "El documento " + document.getIdSubmittedDocument() + " no es de entidad externa");
        }
    }

    private static void aplicarMovement(SubmittedDocument document,
            DtoMovementDocumentEntidadExterna movement) {
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

    private DtoManagementDocumentsEntidadesExternas toDto(DeedManagement management,
            List<SubmittedDocument> documents) {
        return new DtoManagementDocumentsEntidadesExternas(
                management.getIdManagement(),
                management.getNumber(),
                management.getEncabezado(),
                management.getDateStart(),
                nameNotary(management.getFkIdNotaryPerson()),
                resolverCadastralDesignation(management.getIdManagement()),
                documents.stream().map(DocumentEntidadExternaService::toDto).toList());
    }

    private static String nameNotary(Person notary) {
        if (notary == null) {
            return null;
        }
        return (notary.getFirstName() + " " + notary.getLastName()).trim();
    }

    private String resolverCadastralDesignation(Integer idManagement) {
        return procedureRepository.findByFkIdManagementIdManagement(idManagement).stream()
                .map(Procedure::getFkIdProperty)
                .filter(java.util.Objects::nonNull)
                .map(Property::getCadastralDesignation)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static DtoDocumentEntidadExterna toDto(SubmittedDocument document) {
        return new DtoDocumentEntidadExterna(
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
