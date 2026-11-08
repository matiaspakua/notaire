package com.licensis.notaire.application.usecase.document;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.documentcosttemplate.ManageDocumentCostTemplatesUseCase;
import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort;
import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort.CreateResult;

/**
 * Write-side use case for document cost templates — delegates to
 * {@link DocumentCostTemplateOpsPort} (issue #1821).
 */
@Service
public class DocumentCostTemplateWriteService implements ManageDocumentCostTemplatesUseCase {

    private final DocumentCostTemplateOpsPort ops;

    public DocumentCostTemplateWriteService(DocumentCostTemplateOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public CreateResult create(
            Integer idProcedureType,
            Integer idDocumentType,
            BigDecimal fixedAmount,
            BigDecimal variablePercentage) {
        return ops.create(idProcedureType, idDocumentType, fixedAmount, variablePercentage);
    }
}
