package com.licensis.notaire.adapter.out.persistence.budget;

import java.util.List;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;

/**
 * Persistence/ops adapter for budget templates. Holds entity access previously on
 * {@code BudgetTemplateController}; delegates rules/transactions to
 * {@link BudgetTemplateCatalogService} (issue #1803).
 */
@Component
public class BudgetTemplateOpsAdapter implements BudgetTemplateOpsPort {

    private final BudgetTemplateCatalogService catalog;

    public BudgetTemplateOpsAdapter(BudgetTemplateCatalogService catalog) {
        this.catalog = catalog;
    }

    private TemplateView toView(BudgetTemplate entity) {
        BudgetTemplatePK pk = entity.getBudgetTemplatePK();
        return new TemplateView(
                pk != null ? pk.getFkIdProcedureType() : null,
                pk != null ? pk.getFkIdConcept() : null,
                entity.getNotes(),
                entity.getVersion());
    }

    @Override
    public List<TemplateView> findAll() {
        return catalog.findAll().stream().map(this::toView).toList();
    }

    @Override
    public List<TemplateView> findByProcedureType(Integer procedureTypeId) {
        return catalog.findByProcedureType(procedureTypeId).stream().map(this::toView).toList();
    }

    @Override
    public TemplateView create(Integer procedureTypeId, Integer conceptId, String notes) {
        return toView(catalog.create(procedureTypeId, conceptId, notes));
    }

    @Override
    public void updateNotes(Integer procedureTypeId, Integer conceptId, String notes) {
        catalog.updateNotes(new BudgetTemplatePK(procedureTypeId, conceptId), notes);
    }

    @Override
    public void delete(Integer procedureTypeId, Integer conceptId) {
        catalog.delete(new BudgetTemplatePK(procedureTypeId, conceptId));
    }
}
