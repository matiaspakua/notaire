package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.DocumentCostTemplateRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU27/CU39 - Costos (fijos o variables) esperados por tipo de documento dentro
 * de la plantilla de presupuesto de un tipo de trámite (Issue #823).
 */
@Service
@Transactional
public class DocumentCostTemplateService {

    private final DocumentCostTemplateRepository documentCostTemplateRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final DocumentTypeRepository documentTypeRepository;

    public DocumentCostTemplateService(DocumentCostTemplateRepository documentCostTemplateRepository,
                                           ProcedureTypeRepository procedureTypeRepository,
                                           DocumentTypeRepository documentTypeRepository) {
        this.documentCostTemplateRepository = documentCostTemplateRepository;
        this.procedureTypeRepository = procedureTypeRepository;
        this.documentTypeRepository = documentTypeRepository;
    }

    public DocumentCostTemplate create(Integer idProcedureType, Integer idDocumentType,
                                          Float fixedAmount, Float variablePercentage) {
        validateExactlyOneCost(fixedAmount, variablePercentage);

        ProcedureType procedureType = procedureTypeRepository.findById(idProcedureType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el tipo de trámite con ID: " + idProcedureType));
        DocumentType documentType = documentTypeRepository.findById(idDocumentType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el tipo de documento con ID: " + idDocumentType));

        DocumentCostTemplate cost = new DocumentCostTemplate(
                procedureType.getIdProcedureType(), documentType.getIdDocumentType());
        cost.setProcedureType(procedureType);
        cost.setDocumentType(documentType);
        cost.setFixedAmount(fixedAmount);
        cost.setVariablePercentage(variablePercentage);
        return documentCostTemplateRepository.save(cost);
    }

    @Transactional(readOnly = true)
    public List<DocumentCostTemplate> findByTypeProcedure(Integer idProcedureType) {
        return documentCostTemplateRepository.findByProcedureTypeIdProcedureType(idProcedureType);
    }

    private void validateExactlyOneCost(Float fixedAmount, Float variablePercentage) {
        boolean tieneFixedAmount = fixedAmount != null;
        boolean tieneVariablePercentage = variablePercentage != null;
        if (tieneFixedAmount == tieneVariablePercentage) {
            throw new BusinessValidationException(
                    "Debe indicarse exactamente uno entre monto fijo y porcentaje variable");
        }
    }
}
