package com.licensis.notaire.application.port.out.documentcosttemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * Outbound ops port for document cost templates (issue #1821 / CU27/CU39).
 */
public interface DocumentCostTemplateOpsPort {

    record CostTemplateView(
            int procedureTypeId,
            int documentTypeId,
            Integer procedureTypeRefId,
            String procedureTypeName,
            String procedureTypeNotes,
            boolean procedureTypeArchived,
            boolean procedureTypeRegistered,
            boolean procedureTypeAssociatesProperties,
            boolean procedureTypeEnabled,
            Integer documentTypeRefId,
            String documentTypeName,
            boolean documentTypeExpires,
            Integer documentTypeDueDays,
            String documentTypeDeliveredBy,
            boolean documentTypeReturned,
            boolean documentTypeEnabled,
            BigDecimal fixedAmount,
            BigDecimal variablePercentage,
            int version) {}

    sealed interface CreateResult {
        record Created(CostTemplateView view) implements CreateResult {}

        record NotFound(String message) implements CreateResult {}

        record Invalid(String message) implements CreateResult {}
    }

    CreateResult create(
            Integer idProcedureType,
            Integer idDocumentType,
            BigDecimal fixedAmount,
            BigDecimal variablePercentage);

    List<CostTemplateView> findByProcedureType(Integer idProcedureType);
}
