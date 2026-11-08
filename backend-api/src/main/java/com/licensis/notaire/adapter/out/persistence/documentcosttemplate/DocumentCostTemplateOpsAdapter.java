package com.licensis.notaire.adapter.out.persistence.documentcosttemplate;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;

/**
 * Ops adapter for document cost templates. Wraps {@link DocumentCostTemplateService}
 * and maps entities to views (issue #1821).
 */
@Component
public class DocumentCostTemplateOpsAdapter implements DocumentCostTemplateOpsPort {

    private final DocumentCostTemplateService service;

    public DocumentCostTemplateOpsAdapter(DocumentCostTemplateService service) {
        this.service = service;
    }

    @Override
    public CreateResult create(
            Integer idProcedureType,
            Integer idDocumentType,
            BigDecimal fixedAmount,
            BigDecimal variablePercentage) {
        try {
            return new CreateResult.Created(
                    toView(service.create(idProcedureType, idDocumentType, fixedAmount, variablePercentage)));
        } catch (ResourceNotFoundException e) {
            return new CreateResult.NotFound(e.getMessage());
        } catch (BusinessValidationException e) {
            return new CreateResult.Invalid(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostTemplateView> findByProcedureType(Integer idProcedureType) {
        return service.findByTypeProcedure(idProcedureType).stream().map(this::toView).toList();
    }

    private CostTemplateView toView(DocumentCostTemplate cost) {
        ProcedureType procedureType = cost.getProcedureType();
        DocumentType documentType = cost.getDocumentType();
        return new CostTemplateView(
                cost.getDocumentCostTemplatePK().getFkIdProcedureType(),
                cost.getDocumentCostTemplatePK().getFkIdDocumentType(),
                procedureType != null ? procedureType.getIdProcedureType() : null,
                procedureType != null ? procedureType.getName() : null,
                procedureType != null ? procedureType.getNotes() : null,
                procedureType != null && Boolean.TRUE.equals(procedureType.getIsArchived()),
                procedureType != null && Boolean.TRUE.equals(procedureType.getIsRegistered()),
                procedureType != null && Boolean.TRUE.equals(procedureType.getAssociatesProperties()),
                procedureType != null && Boolean.TRUE.equals(procedureType.getEnabled()),
                documentType != null ? documentType.getIdDocumentType() : null,
                documentType != null ? documentType.getName() : null,
                documentType != null && Boolean.TRUE.equals(documentType.getExpires()),
                documentType != null ? documentType.getDueDays() : null,
                documentType != null ? documentType.getDeliveredBy() : null,
                documentType != null && Boolean.TRUE.equals(documentType.getReturned()),
                documentType != null && Boolean.TRUE.equals(documentType.getEnabled()),
                cost.getFixedAmount(),
                cost.getVariablePercentage(),
                cost.getVersion());
    }
}
