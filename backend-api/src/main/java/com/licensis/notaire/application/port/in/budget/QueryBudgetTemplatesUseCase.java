package com.licensis.notaire.application.port.in.budget;

import java.util.List;

import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort.TemplateView;

/**
 * Read-side inbound port for budget templates (issue #1803).
 */
public interface QueryBudgetTemplatesUseCase {

    List<TemplateView> findAll();

    List<TemplateView> findByProcedureType(Integer procedureTypeId);
}
