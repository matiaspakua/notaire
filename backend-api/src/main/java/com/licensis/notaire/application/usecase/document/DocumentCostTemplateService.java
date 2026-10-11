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
 * CU27/CU39 - Costos (fijos or variables) esperados by document type dentro
 * of the budget template of a procedure type (Issue #823).
 */
@Service
@Transactional
public class DocumentCostTemplateService {

    private final DocumentRepositoryPort documentRepository;

    public DocumentCostTemplateService(DocumentRepositoryPort documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentCostTemplate create(Integer idProcedureType, Integer idDocumentType,
                                          java.math.BigDecimal fixedAmount, java.math.BigDecimal variablePercentage) {
        validateExactlyOneCost(fixedAmount, variablePercentage);

        ProcedureType procedureType = documentRepository.findProcedureTypeById(idProcedureType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Procedure type not found with ID: " + idProcedureType));
        DocumentType documentType = documentRepository.findDocumentTypeById(idDocumentType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document type not found with ID: " + idDocumentType));

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

    private void validateExactlyOneCost(java.math.BigDecimal fixedAmount, java.math.BigDecimal variablePercentage) {
        boolean hasFixedAmount = fixedAmount != null;
        boolean hasVariablePercentage = variablePercentage != null;
        if (hasFixedAmount == hasVariablePercentage) {
            throw new BusinessValidationException(
                    "Exactly one of fixed amount and variable percentage must be provided");
        }
    }
}
