package com.licensis.notaire.application.port.in.budget;

import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort.TemplateView;

/**
 * Write-side inbound port for budget templates (issue #1803).
 */
public interface ManageBudgetTemplatesUseCase {

    TemplateView create(Integer procedureTypeId, Integer conceptId, String notes);

    void updateNotes(Integer procedureTypeId, Integer conceptId, String notes);

    void delete(Integer procedureTypeId, Integer conceptId);
}
