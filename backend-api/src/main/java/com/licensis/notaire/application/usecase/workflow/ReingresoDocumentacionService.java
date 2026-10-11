package com.licensis.notaire.application.usecase.workflow;

import com.licensis.notaire.dto.DtoRequiredDocument;
import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoManagementDocumentReentry;
import com.licensis.notaire.dto.DtoDocumentReentryRequest;
import com.licensis.notaire.dto.DtoProcedureRequiredDocumentation;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU43 — Re-enter documentation. For a management, lists each procedure and the
 * documents its {@code ProcedureTemplate} requires, and re-enters a document
 * type by creating a {@code SubmittedDocument} with {@code reingresado=true}.
 */
@Service
public class ReingresoDocumentacionService {

    private final DeedManagementRepository managementRepository;
    private final ProcedureRepository procedureRepository;
    private final ProcedureTemplateRepository procedureTemplateRepository;
    private final SubmittedDocumentRepository submittedDocumentRepository;

    public ReingresoDocumentacionService(DeedManagementRepository managementRepository,
            ProcedureRepository procedureRepository, ProcedureTemplateRepository procedureTemplateRepository,
            SubmittedDocumentRepository submittedDocumentRepository) {
        this.managementRepository = managementRepository;
        this.procedureRepository = procedureRepository;
        this.procedureTemplateRepository = procedureTemplateRepository;
        this.submittedDocumentRepository = submittedDocumentRepository;
    }

    @Transactional(readOnly = true)
    public DtoManagementDocumentReentry getRequiredDocumentation(Integer idManagement) {
        DeedManagement management = findManagementOrThrow(idManagement);
        List<DtoProcedureRequiredDocumentation> procedures = procedureRepository
                .findByFkIdManagementIdManagement(idManagement).stream()
                .map(this::toDtoProcedure)
                .toList();
        return new DtoManagementDocumentReentry(management.getIdManagement(), management.getNumber(),
                management.getHeading(), procedures);
    }

    @Transactional
    public DtoDocumentReentered reenter(Integer idManagement, DtoDocumentReentryRequest request) {
        findManagementOrThrow(idManagement);
        Procedure procedure = procedureRepository.findById(request.idProcedure())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Procedure not found with ID: " + request.idProcedure()));
        validateBelongsToManagement(procedure, idManagement);
        DocumentType typeDocument = validateRequiredDocumentation(procedure, request.idDocumentType());

        SubmittedDocument document = createSubmittedDocument(procedure, typeDocument);
        SubmittedDocument guardado = submittedDocumentRepository.save(document);

        return toDto(guardado);
    }

    private DeedManagement findManagementOrThrow(Integer idManagement) {
        return managementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException("Management not found with ID: " + idManagement));
    }

    private static void validateBelongsToManagement(Procedure procedure, Integer idManagement) {
        DeedManagement management = procedure.getFkIdManagement();
        if (management == null || !idManagement.equals(management.getIdManagement())) {
            throw new BusinessValidationException(
                    "The procedure " + procedure.getIdProcedure() + " does not belong to management " + idManagement);
        }
    }

    private DocumentType validateRequiredDocumentation(Procedure procedure, Integer idDocumentType) {
        Integer idProcedureType = procedure.getFkIdProcedureType().getIdProcedureType();
        ProcedureTemplatePK pk = new ProcedureTemplatePK(idProcedureType, idDocumentType);
        ProcedureTemplate template = procedureTemplateRepository.findById(pk)
                .orElseThrow(() -> new BusinessValidationException(
                        "Document type " + idDocumentType
                                + " is not part of the documentation required for procedure "
                                + procedure.getIdProcedure()));
        return template.getDocumentType();
    }

    private static SubmittedDocument createSubmittedDocument(Procedure procedure, DocumentType typeDocument) {
        SubmittedDocument document = new SubmittedDocument();
        document.setFkIdProcedure(procedure);
        document.setDocumentType(typeDocument);
        document.setName(typeDocument.getName());
        document.setExpires(typeDocument.getExpires());
        document.setDueDays(typeDocument.getDueDays());
        document.setDeliveredBy(typeDocument.getDeliveredBy());
        document.setReentered(true);
        // released/flagged are NOT NULL in Postgres; a freshly re-entered
        // document starts as neither released nor flagged.
        document.setReleased(false);
        document.setFlagged(false);
        return document;
    }

    private DtoProcedureRequiredDocumentation toDtoProcedure(Procedure procedure) {
        Integer idProcedureType = procedure.getFkIdProcedureType().getIdProcedureType();
        List<DtoRequiredDocument> documents = procedureTemplateRepository
                .findByProcedureTypeIdProcedureType(idProcedureType).stream()
                .map(template -> toDtoDocumentNecesario(template.getDocumentType()))
                .toList();
        return new DtoProcedureRequiredDocumentation(procedure.getIdProcedure(),
                procedure.getFkIdProcedureType().getName(), documents);
    }

    private static DtoRequiredDocument toDtoDocumentNecesario(DocumentType typeDocument) {
        return new DtoRequiredDocument(typeDocument.getIdDocumentType(), typeDocument.getName(),
                typeDocument.getExpires(), typeDocument.getDueDays(), typeDocument.getDeliveredBy());
    }

    private static DtoDocumentReentered toDto(SubmittedDocument document) {
        return new DtoDocumentReentered(
                document.getIdSubmittedDocument(),
                document.getFkIdProcedure().getIdProcedure(),
                document.getFkIdDocumentType(),
                document.getName(),
                document.getExpires(),
                document.getDueDays(),
                document.getDeliveredBy(),
                Boolean.TRUE.equals(document.getReentered()));
    }
}
