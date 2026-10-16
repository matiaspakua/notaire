package com.licensis.notaire.adapter.out.persistence.documentcosttemplate;

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
 * CU27/CU39 — Expected costs (fixed or variable) by document type inside
 * the budget template of a procedure type. Outbound persistence helper (issue #1866).
 */
@Service
@Transactional
public class DocumentCostTemplateService {

    private final DocumentCostTemplateRepository costTemplateRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final ProcedureTypeRepository procedureTypeRepository;

    public DocumentCostTemplateService(
            DocumentCostTemplateRepository costTemplateRepository,
            DocumentTypeRepository documentTypeRepository,
            ProcedureTypeRepository procedureTypeRepository) {
        this.costTemplateRepository = costTemplateRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.procedureTypeRepository = procedureTypeRepository;
    }

    public DocumentCostTemplate create(Integer idProcedureType, Integer idDocumentType,
                                          java.math.BigDecimal fixedAmount, java.math.BigDecimal variablePercentage) {
        validateExactlyOneCost(fixedAmount, variablePercentage);

        ProcedureType procedureType = procedureTypeRepository.findById(idProcedureType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Procedure type not found with ID: " + idProcedureType));
        DocumentType documentType = documentTypeRepository.findById(idDocumentType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document type not found with ID: " + idDocumentType));

        DocumentCostTemplate cost = new DocumentCostTemplate(
                procedureType.getIdProcedureType(), documentType.getIdDocumentType());
        cost.setProcedureType(procedureType);
        cost.setDocumentType(documentType);
        cost.setFixedAmount(fixedAmount);
        cost.setVariablePercentage(variablePercentage);
        return costTemplateRepository.save(cost);
    }

    @Transactional(readOnly = true)
    public List<DocumentCostTemplate> findByTypeProcedure(Integer idProcedureType) {
        return costTemplateRepository.findByProcedureTypeIdProcedureType(idProcedureType);
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
