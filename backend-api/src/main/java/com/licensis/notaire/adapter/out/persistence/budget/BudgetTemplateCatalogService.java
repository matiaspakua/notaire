package com.licensis.notaire.adapter.out.persistence.budget;

import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.ConceptRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages the concepts a procedure type pre-loads into a budget (CU39, CU49, CU55). Replaces the
 * controller's per-call legacy JPA controller built from a static EntityManagerFactory (#574).
 */
@Service
@Transactional
public class BudgetTemplateCatalogService {

    private static final int CONFLICT = 409;

    private final BudgetTemplateRepository budgetTemplateRepository;
    private final ConceptRepository conceptRepository;
    private final ProcedureTypeRepository procedureTypeRepository;

    public BudgetTemplateCatalogService(BudgetTemplateRepository budgetTemplateRepository,
            ConceptRepository conceptRepository, ProcedureTypeRepository procedureTypeRepository) {
        this.budgetTemplateRepository = budgetTemplateRepository;
        this.conceptRepository = conceptRepository;
        this.procedureTypeRepository = procedureTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetTemplate> findAll() {
        return budgetTemplateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BudgetTemplate> findByProcedureType(Integer idProcedureType) {
        return budgetTemplateRepository.findByProcedureTypeIdProcedureType(idProcedureType);
    }

    public BudgetTemplate create(Integer idProcedureType, Integer idConcept, String notes) {
        if (idProcedureType == null || idConcept == null) {
            throw new BusinessValidationException("fkIdProcedureType and fkIdConcept are required");
        }
        if (!conceptRepository.existsById(idConcept) || !procedureTypeRepository.existsById(idProcedureType)) {
            throw new BusinessValidationException("concept or procedure type not found");
        }
        BudgetTemplatePK key = new BudgetTemplatePK(idProcedureType, idConcept);
        if (budgetTemplateRepository.existsById(key)) {
            throw new NotaireException(CONFLICT,
                    "The budget template already exists for that procedure type and concept");
        }
        BudgetTemplate template = new BudgetTemplate(key);
        template.setNotes(notes);
        template.setConcept(conceptRepository.getReferenceById(idConcept));
        template.setProcedureType(procedureTypeRepository.getReferenceById(idProcedureType));
        return budgetTemplateRepository.save(template);
    }

    public void updateNotes(BudgetTemplatePK key, String notes) {
        BudgetTemplate template = budgetTemplateRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Budget template not found: " + key));
        if (notes != null) {
            template.setNotes(notes);
        }
    }

    public void delete(BudgetTemplatePK key) {
        BudgetTemplate template = budgetTemplateRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Budget template not found: " + key));
        // The parents cascade ALL over their template lists: a template left in either list is
        // re-persisted on flush, which silently undoes the delete.
        template.getProcedureType().getBudgetTemplateList().remove(template);
        template.getConcept().getBudgetTemplateList().remove(template);
        budgetTemplateRepository.delete(template);
    }
}
