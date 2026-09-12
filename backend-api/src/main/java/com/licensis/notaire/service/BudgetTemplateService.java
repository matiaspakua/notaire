package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loads the Conceptos of a TipoDeTramite's PlantillaPresupuesto as Items of a
 * real Presupuesto (CU39).
 */
@Service
@Transactional
public class BudgetTemplateService {

    private final BudgetRepository budgetRepository;
    private final BudgetTemplateRepository budgetTemplateRepository;
    private final ItemRepository itemRepository;

    public BudgetTemplateService(BudgetRepository budgetRepository,
            BudgetTemplateRepository budgetTemplateRepository,
            ItemRepository itemRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetTemplateRepository = budgetTemplateRepository;
        this.itemRepository = itemRepository;
    }

    public List<Item> cargarItemsDesdeTemplate(Integer idBudget, Integer typeProcedureId) {
        Budget budget = budgetRepository.findById(idBudget)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con id: " + idBudget));

        List<BudgetTemplate> plantillas =
                budgetTemplateRepository.findByProcedureTypeIdProcedureType(typeProcedureId);

        if (plantillas.isEmpty()) {
            throw new BusinessValidationException(
                    "El tipo de trámite no tiene plantilla configurada");
        }

        List<Item> items = plantillas.stream()
                .map(template -> crearItemDesdeConcept(budget, template.getConcept()))
                .toList();

        return itemRepository.saveAll(items);
    }

    private Item crearItemDesdeConcept(Budget budget, Concept concept) {
        Item item = new Item();
        item.setName(concept.getName());
        item.setValue(concept.getValue());
        item.setPercentage(concept.getPercentage());
        item.setFkIdBudget(budget);
        return item;
    }
}
