package com.licensis.notaire.application.port.in.documentcosttemplate;

import java.math.BigDecimal;

import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort.CreateResult;

/** Write-side inbound port for document cost templates (issue #1821). */
public interface ManageDocumentCostTemplatesUseCase {

    CreateResult create(
            Integer idProcedureType,
            Integer idDocumentType,
            BigDecimal fixedAmount,
            BigDecimal variablePercentage);
}
