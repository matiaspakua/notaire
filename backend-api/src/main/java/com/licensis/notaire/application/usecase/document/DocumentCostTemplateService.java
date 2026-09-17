package com.licensis.notaire.application.usecase.document;

import com.licensis.notaire.application.port.out.document.DocumentRepositoryPort;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
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

    private final DocumentRepositoryPort documentRepository;

    public DocumentCostTemplateService(DocumentRepositoryPort documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentCostTemplate create(Integer idProcedureType, Integer idDocumentType,
                                          Float fixedAmount, Float variablePercentage) {
        validateExactlyOneCost(fixedAmount, variablePercentage);

        ProcedureType procedureType = documentRepository.findProcedureTypeById(idProcedureType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el tipo de trámite con ID: " + idProcedureType));
        DocumentType documentType = documentRepository.findDocumentTypeById(idDocumentType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el tipo de documento con ID: " + idDocumentType));

        DocumentCostTemplate cost = new DocumentCostTemplate(
                procedureType.getIdProcedureType(), documentType.getIdDocumentType());
        cost.setProcedureType(procedureType);
        cost.setDocumentType(documentType);
        cost.setFixedAmount(fixedAmount);
        cost.setVariablePercentage(variablePercentage);
        return documentRepository.saveCostTemplate(cost);
    }

    @Transactional(readOnly = true)
    public List<DocumentCostTemplate> findByTypeProcedure(Integer idProcedureType) {
        return documentRepository.findCostTemplatesByProcedureType(idProcedureType);
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
