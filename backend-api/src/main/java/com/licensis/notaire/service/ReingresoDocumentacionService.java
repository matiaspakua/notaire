package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoDocumentNecesario;
import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoManagementReingresoDocumentacion;
import com.licensis.notaire.dto.DtoReingresoDocumentacionRequest;
import com.licensis.notaire.dto.DtoProcedureDocumentacionNecesaria;
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
 * CU43 - Reingresar documentación: expone, para una gestión, sus trámites
 * junto con la documentación necesaria de cada uno (según su
 * {@code PlantillaTramite}), y permite reingresar un tipo de documento
 * creando un nuevo {@code DocumentoPresentado} con {@code reingresado=true}.
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
    public DtoManagementReingresoDocumentacion obtenerDocumentacionNecesaria(Integer idManagement) {
        DeedManagement management = findManagementOrThrow(idManagement);
        List<DtoProcedureDocumentacionNecesaria> procedures = procedureRepository
                .findByFkIdManagementIdManagement(idManagement).stream()
                .map(this::toDtoProcedure)
                .toList();
        return new DtoManagementReingresoDocumentacion(management.getIdManagement(), management.getNumber(),
                management.getEncabezado(), procedures);
    }

    @Transactional
    public DtoDocumentReentered reingresar(Integer idManagement, DtoReingresoDocumentacionRequest request) {
        findManagementOrThrow(idManagement);
        Procedure procedure = procedureRepository.findById(request.idProcedure())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trámite no encontrado con ID: " + request.idProcedure()));
        validarPerteneceAManagement(procedure, idManagement);
        DocumentType typeDocument = validarDocumentacionNecesaria(procedure, request.idDocumentType());

        SubmittedDocument document = crearSubmittedDocument(procedure, typeDocument);
        SubmittedDocument guardado = submittedDocumentRepository.save(document);

        return toDto(guardado);
    }

    private DeedManagement findManagementOrThrow(Integer idManagement) {
        return managementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idManagement));
    }

    private static void validarPerteneceAManagement(Procedure procedure, Integer idManagement) {
        DeedManagement management = procedure.getFkIdManagement();
        if (management == null || !idManagement.equals(management.getIdManagement())) {
            throw new BusinessValidationException(
                    "El trámite " + procedure.getIdProcedure() + " no pertenece a la gestión " + idManagement);
        }
    }

    private DocumentType validarDocumentacionNecesaria(Procedure procedure, Integer idDocumentType) {
        Integer idProcedureType = procedure.getFkIdProcedureType().getIdProcedureType();
        ProcedureTemplatePK pk = new ProcedureTemplatePK(idProcedureType, idDocumentType);
        ProcedureTemplate template = procedureTemplateRepository.findById(pk)
                .orElseThrow(() -> new BusinessValidationException(
                        "El tipo de documento " + idDocumentType
                                + " no forma parte de la documentación necesaria del trámite "
                                + procedure.getIdProcedure()));
        return template.getDocumentType();
    }

    private static SubmittedDocument crearSubmittedDocument(Procedure procedure, DocumentType typeDocument) {
        SubmittedDocument document = new SubmittedDocument();
        document.setFkIdProcedure(procedure);
        document.setFkIdDocumentType(typeDocument.getIdDocumentType());
        document.setName(typeDocument.getName());
        document.setExpires(typeDocument.getExpires());
        document.setDueDays(typeDocument.getDueDays());
        document.setDeliveredBy(typeDocument.getDeliveredBy());
        document.setReentered(true);
        // liberado/observado are NOT NULL in Postgres; a freshly reingresado
        // document starts as neither liberado nor observado.
        document.setReleased(false);
        document.setFlagged(false);
        return document;
    }

    private DtoProcedureDocumentacionNecesaria toDtoProcedure(Procedure procedure) {
        Integer idProcedureType = procedure.getFkIdProcedureType().getIdProcedureType();
        List<DtoDocumentNecesario> documents = procedureTemplateRepository
                .findByProcedureTypeIdProcedureType(idProcedureType).stream()
                .map(template -> toDtoDocumentNecesario(template.getDocumentType()))
                .toList();
        return new DtoProcedureDocumentacionNecesaria(procedure.getIdProcedure(),
                procedure.getFkIdProcedureType().getName(), documents);
    }

    private static DtoDocumentNecesario toDtoDocumentNecesario(DocumentType typeDocument) {
        return new DtoDocumentNecesario(typeDocument.getIdDocumentType(), typeDocument.getName(),
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
