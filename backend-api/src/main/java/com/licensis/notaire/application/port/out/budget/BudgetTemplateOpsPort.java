package com.licensis.notaire.application.port.out.budget;

import java.util.List;

/**
 * Outbound persistence/ops port for budget templates (issue #1803).
 * Exposes view shapes only so the application layer stays free of entities.
 */
public interface BudgetTemplateOpsPort {

    record TemplateView(Integer procedureTypeId, Integer conceptId, String notes, int version) {}

    List<TemplateView> findAll();

    List<TemplateView> findByProcedureType(Integer procedureTypeId);

    TemplateView create(Integer procedureTypeId, Integer conceptId, String notes);

    void updateNotes(Integer procedureTypeId, Integer conceptId, String notes);

    void delete(Integer procedureTypeId, Integer conceptId);
}
