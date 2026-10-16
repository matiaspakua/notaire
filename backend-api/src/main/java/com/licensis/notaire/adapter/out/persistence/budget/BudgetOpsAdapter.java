package com.licensis.notaire.adapter.out.persistence.budget;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.budget.BudgetOpsPort;
import com.licensis.notaire.application.usecase.budget.BudgetService;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.BudgetStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.PersonRepository;

/**
 * Persistence/ops adapter for budgets. Holds entity/PersonRepository access previously on
 * {@code BudgetController} (issue #1805).
 */
@Component
public class BudgetOpsAdapter implements BudgetOpsPort {

    private final BudgetService budgetService;
    private final PersonRepository personRepository;

    public BudgetOpsAdapter(BudgetService budgetService, PersonRepository personRepository) {
        this.budgetService = budgetService;
        this.personRepository = personRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        var pageable = PageRequest.of(page, size, parseSort(sort));
        var result = budgetService.findAllPaged(pageable).map(this::toView);
        return new PageResult(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BudgetView> findById(Integer id) {
        return budgetService.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetView> findByPerson(Integer personId) {
        return budgetService.findByPerson(personId).stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetView> findByStatus(String status) {
        return budgetService.findByStatus(canonicalStatus(status)).stream().map(this::toView).toList();
    }

    @Override
    public SaveResult create(BudgetCommand command) {
        Budget entity = new Budget();
        applyCommand(entity, command);
        return new SaveResult.Created(toView(budgetService.create(entity)));
    }

    @Override
    public SaveResult update(Integer id, BudgetCommand command) {
        try {
            return budgetService.findById(id).<SaveResult>map(existing -> {
                applyCommand(existing, command);
                return new SaveResult.Updated(toView(budgetService.update(id, existing)));
            }).orElseGet(SaveResult.NotFound::new);
        } catch (ResourceNotFoundException e) {
            return new SaveResult.NotFound();
        }
    }

    @Override
    public DeleteResult delete(Integer id) {
        try {
            budgetService.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (ResourceNotFoundException e) {
            return new DeleteResult.NotFound();
        }
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "idBudget");
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }

    /**
     * The {@link BudgetStatus} code for a value in any letter case and spacing, or a 400 naming
     * the accepted values (#1346). {@code null} stays {@code null}.
     */
    private static String canonicalStatus(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (BudgetStatus status : BudgetStatus.values()) {
            if (status.name().equalsIgnoreCase(trimmed)) {
                return status.name();
            }
        }
        StringBuilder accepted = new StringBuilder();
        for (BudgetStatus status : BudgetStatus.values()) {
            accepted.append(accepted.isEmpty() ? "" : ", ").append(status.name());
        }
        throw new BusinessValidationException(
                "status: invalid value '" + value + "'; accepted values: " + accepted);
    }

    private void applyCommand(Budget budget, BudgetCommand command) {
        if (command.number() != null) {
            budget.setNumber(command.number());
        }
        if (command.date() != null) {
            budget.setDate(command.date());
        }
        budget.setHeading(command.heading());
        budget.setStatus(canonicalStatus(command.status()));
        budget.setPropertyAmount(command.propertyAmount());
        budget.setNotes(command.notes());
        Integer personId = command.personId();
        if (personId != null) {
            Person person = personRepository.findById(personId).orElse(null);
            budget.setFkIdPerson(person);
        } else {
            budget.setFkIdPerson(null);
        }
    }

    private BudgetView toView(Budget budget) {
        PersonView person = null;
        Person linked = budget.getFkIdPerson();
        if (linked != null && linked.getPersonId() != null) {
            person = new PersonView(linked.getPersonId(), linked.getFirstName(), linked.getLastName());
        }
        return new BudgetView(
                budget.getIdBudget(),
                budget.getNumber(),
                budget.getDate(),
                budget.getHeading(),
                budget.getStatus(),
                budget.getPropertyAmount(),
                budget.getNotes(),
                person,
                budget.getVersion());
    }
}
