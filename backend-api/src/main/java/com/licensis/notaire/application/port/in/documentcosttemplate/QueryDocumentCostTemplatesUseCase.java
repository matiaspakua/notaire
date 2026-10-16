package com.licensis.notaire.application.port.in.documentcosttemplate;

import java.util.List;

import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort.CostTemplateView;

/** Query-side inbound port for document cost templates (issue #1821). */
public interface QueryDocumentCostTemplatesUseCase {

    List<CostTemplateView> findByProcedureType(Integer idProcedureType);
}
