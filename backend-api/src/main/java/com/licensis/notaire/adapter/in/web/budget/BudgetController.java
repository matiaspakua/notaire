package com.licensis.notaire.adapter.in.web.budget;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.adapter.in.web.payment.PaymentWebMapper;
import com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase;
import com.licensis.notaire.dto.DtoBudgetSummary;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.adapter.in.web.item.ItemController.ItemResponse;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.BudgetStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.application.usecase.budget.BudgetCatalogItemsService;
import com.licensis.notaire.application.usecase.budget.BudgetTemplateService;
import com.licensis.notaire.application.usecase.budget.BudgetService;
import com.licensis.notaire.repository.PersonRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budgets", description = "API for managing budgets")
public class BudgetController {

    private static final Logger log = LoggerFactory.getLogger(BudgetController.class);

/**
     * Nested client on budget payloads. Wire keys match frontend {@code DtoPerson}:
     * {@code personId}, {@code name}, {@code lastName}.
     */
    record PersonRef(Integer personId, String name, String lastName) {}

    record BudgetRequest(
            Integer number,
            Date date,
            String heading,
            @NotBlank
            @Schema(allowableValues = {"BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "FACTURADO"},
                    description = "Budget status (#1346). Accepts any uppercase/lowercase and "
                            + "guarda the código; another valor responde 400.")
            String status,
            @JsonAlias("amount") java.math.BigDecimal propertyAmount,
            String notes,
            Integer personId,
            @JsonProperty("person") PersonRef person) {}

    // NON_NULL so omitted client association is absent (not "person": null) — matches CU01 /
    // frontend Budget.person?: DtoPerson and BudgetPersonAssociationPgIntegrationTest.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record BudgetResponse(
            Integer idBudget,
            int number,
            Date date,
            String heading,
            String status,
            java.math.BigDecimal propertyAmount,
            String notes,
            PersonRef person,
            int version) {}

    private final BudgetService budgetService;
    private final GetBudgetSummaryUseCase budgetSummaryUseCase;
    private final BudgetTemplateService budgetTemplateService;
    private final BudgetCatalogItemsService budgetCatalogoItemsService;
    private final PersonRepository personRepository;

    public BudgetController(BudgetService budgetService,
            GetBudgetSummaryUseCase budgetSummaryUseCase,
            BudgetTemplateService budgetTemplateService,
            BudgetCatalogItemsService budgetCatalogoItemsService,
            PersonRepository personRepository) {
        this.budgetService = budgetService;
        this.budgetSummaryUseCase = budgetSummaryUseCase;
        this.budgetTemplateService = budgetTemplateService;
        this.budgetCatalogoItemsService = budgetCatalogoItemsService;
        this.personRepository = personRepository;
    }

    private Integer resolvePersonId(BudgetRequest request) {
        if (request.personId() != null) {
            return request.personId();
        }
        return request.person() != null ? request.person().personId() : null;
    }

    private BudgetResponse toResponse(Budget budget) {
        PersonRef person = null;
        Person linked = budget.getFkIdPerson();
        if (linked != null && linked.getPersonId() != null) {
            person = new PersonRef(linked.getPersonId(), linked.getFirstName(), linked.getLastName());
        }
        return new BudgetResponse(
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

    /**
     * The {@link BudgetStatus} code for a value in any letter case and spacing, or a 400 naming
     * the accepted values (#1346). {@code null} stays {@code null}.
     */
    private static String canonicalStatus(String value) {
        if (value == null) {
            return null;
        }
        for (BudgetStatus status : BudgetStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status.name();
            }
        }
        return RequiredFields.parseEnum(value, BudgetStatus.class, "status").name(); // throws the 400
    }

    private void applyRequest(Budget budget, BudgetRequest request) {
        if (request.number() != null) {
            budget.setNumber(request.number());
        }
        if (request.date() != null) {
            budget.setDate(request.date());
        }
        budget.setHeading(request.heading());
        budget.setStatus(canonicalStatus(request.status()));
        budget.setPropertyAmount(request.propertyAmount());
        budget.setNotes(request.notes());
        Integer personId = resolvePersonId(request);
        if (personId != null) {
            Person person = personRepository.findById(personId).orElse(null);
            budget.setFkIdPerson(person);
        } else {
            budget.setFkIdPerson(null);
        }
    }

    @GetMapping
    @Operation(summary = "Get budgets, paged",
            description = "Parameters: page (default 0), size (default 20), sort (for example idBudget,desc)")
    public ResponseEntity<Page<BudgetResponse>> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "idBudget", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(budgetService.findAllPaged(pageable).map(this::toResponse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<BudgetResponse> getById(@PathVariable Integer id) {
        return budgetService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}/summary")
    @Operation(summary = "CU47 - Get financial summary of a budget (total, balance and payments)")
    public ResponseEntity<DtoBudgetSummary> getBudgetSummary(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(PaymentWebMapper.toDto(budgetSummaryUseCase.summary(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/person/{idPerson}")
    @Operation(summary = "Get budgets of a person (CU60)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BudgetResponse>> getByPerson(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(budgetService.findByPerson(idPerson).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Status outside the vocabulary")
})
    @GetMapping("/search")
    @Operation(summary = "Search budgets by status (CU60)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BudgetResponse>> search(
            @Parameter(description = "Budget status (any uppercase/lowercase; another valor responde 400)",
                    schema = @Schema(allowableValues = {"BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "FACTURADO"})) @RequestParam(required = false) String status) {
        return ResponseEntity.ok(budgetService.findByStatus(canonicalStatus(status)).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create budget")
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        Budget entity = new Budget();
        applyRequest(entity, request);
        Budget saved = budgetService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Invalid request (p. ej. status outside the vocabulary)"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update budget")
    public ResponseEntity<BudgetResponse> update(@PathVariable Integer id, @Valid @RequestBody BudgetRequest request) {
        try {
            return budgetService.findById(id)
                    .map(existing -> {
                        applyRequest(existing, request);
                        Budget updated = budgetService.update(id, existing);
                        return ResponseEntity.ok(toResponse(updated));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            budgetService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "The procedure type does not have template configured"),
    @ApiResponse(responseCode = "404", description = "Budget not found")
})
    @PostMapping("/{id}/items-from-template")
    @Operation(summary = "CU39 - Load budget items from the template of the procedure type")
    public ResponseEntity<List<ItemResponse>> loadItemsFromTemplate(
            @PathVariable Integer id,
            @Parameter(description = "ID of the procedure type")
            @RequestParam("procedureTypeId") Integer typeProcedureId) {
        return ResponseEntity.ok(budgetTemplateService.loadItemsFromTemplate(id, typeProcedureId).stream()
                .map(ItemResponse::from).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Budget or catalog item not found")
})
    @PostMapping("/{id}/items-from-catalog")
    @Operation(summary = "CU71 - Add catalog item copies to the budget")
    public ResponseEntity<List<ItemResponse>> addItemsFromCatalog(
            @PathVariable Integer id,
            @RequestBody List<Integer> idItems) {
        return ResponseEntity.ok(budgetCatalogoItemsService.addItemsFromCatalog(id, idItems).stream()
                .map(ItemResponse::from).toList());
    }
}
