package com.licensis.notaire.application.usecase.document;

import java.util.List;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.documentcosttemplate.QueryDocumentCostTemplatesUseCase;
import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort;
import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort.CostTemplateView;

/**
 * Query-side use case for document cost templates — delegates to
 * {@link DocumentCostTemplateOpsPort} (issue #1821).
 */
@Service
public class DocumentCostTemplateQueryService implements QueryDocumentCostTemplatesUseCase {

    private final DocumentCostTemplateOpsPort ops;

    public DocumentCostTemplateQueryService(DocumentCostTemplateOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public List<CostTemplateView> findByProcedureType(Integer idProcedureType) {
        return ops.findByProcedureType(idProcedureType);
    }
}
