package com.licensis.notaire.adapter.in.web.budget;

import java.util.Date;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.adapter.in.web.item.ItemResponse;
import com.licensis.notaire.adapter.in.web.support.IllegalArgumentResponses;
import com.licensis.notaire.adapter.in.web.payment.PaymentWebMapper;
import com.licensis.notaire.application.port.in.budget.ManageBudgetsUseCase;
import com.licensis.notaire.application.port.in.budget.QueryBudgetsUseCase;
import com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetCommand;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetView;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.PersonView;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.SaveResult;
import com.licensis.notaire.application.port.in.budget.AddBudgetItemsFromCatalogUseCase;
import com.licensis.notaire.application.port.in.budget.LoadBudgetItemsFromTemplateUseCase;
import com.licensis.notaire.dto.DtoBudgetSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Budgets. CRUD/search call inbound ports only; person hydration and status canonicalization
 * live behind {@code BudgetOpsPort} (issue #1805). Template/catalog item endpoints keep their
 * existing application services.
 */
@RestController
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budgets", description = "API for managing budgets")
public class BudgetController {

    /**
     * Nested client on budget payloads. Wire keys match frontend {@code DtoPerson}:
     * {@code personId}, {@code name}, {@code lastName}.
     */
    record PersonRef(Integer personId, String name, String lastName) { }

    record BudgetRequest(
            Integer number,
            Date date,
            String heading,
            @NotBlank
            @Schema(allowableValues = {"BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "FACTURADO"},
                    description = "Budget status (#1346). Accepts any uppercase/lowercase and "
                            + "stores the code; any other value returns 400.")
            String status,
            @JsonAlias("amount") java.math.BigDecimal propertyAmount,
            String notes,
            Integer personId,
            @JsonProperty("person") PersonRef person) { }

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
            int version) { }

    private final QueryBudgetsUseCase queryBudgets;
    private final ManageBudgetsUseCase manageBudgets;
    private final GetBudgetSummaryUseCase budgetSummaryUseCase;
    private final LoadBudgetItemsFromTemplateUseCase loadBudgetItemsFromTemplate;
    private final AddBudgetItemsFromCatalogUseCase addBudgetItemsFromCatalog;

    public BudgetController(
            QueryBudgetsUseCase queryBudgets,
            ManageBudgetsUseCase manageBudgets,
            GetBudgetSummaryUseCase budgetSummaryUseCase,
            LoadBudgetItemsFromTemplateUseCase loadBudgetItemsFromTemplate,
            AddBudgetItemsFromCatalogUseCase addBudgetItemsFromCatalog) {
        this.queryBudgets = queryBudgets;
        this.manageBudgets = manageBudgets;
        this.budgetSummaryUseCase = budgetSummaryUseCase;
        this.loadBudgetItemsFromTemplate = loadBudgetItemsFromTemplate;
        this.addBudgetItemsFromCatalog = addBudgetItemsFromCatalog;
    }

    private Integer resolvePersonId(BudgetRequest request) {
        if (request.personId() != null) {
            return request.personId();
        }
        return request.person() != null ? request.person().personId() : null;
    }

    private BudgetCommand toCommand(BudgetRequest request) {
        return new BudgetCommand(
                request.number(),
                request.date(),
                request.heading(),
                request.status(),
                request.propertyAmount(),
                request.notes(),
                resolvePersonId(request));
    }

    private BudgetResponse toResponse(BudgetView view) {
        PersonView linked = view.person();
        PersonRef person = linked == null
                ? null
                : new PersonRef(linked.personId(), linked.name(), linked.lastName());
        return new BudgetResponse(
                view.idBudget(),
                view.number(),
                view.date(),
                view.heading(),
                view.status(),
                view.propertyAmount(),
                view.notes(),
                person,
                view.version());
    }

    @GetMapping
    @Operation(summary = "Get budgets, paged",
            description = "Parameters: page (default 0), size (default 20), sort (for example idBudget,desc)")
    public ResponseEntity<Page<BudgetResponse>> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "idBudget", direction = Sort.Direction.DESC)
            Pageable pageable) {
        var order = pageable.getSort().stream().findFirst().orElse(null);
        String sort = order == null
                ? "idBudget,desc"
                : order.getProperty() + "," + order.getDirection().name().toLowerCase();
        var result = queryBudgets.findAll(pageable.getPageNumber(), pageable.getPageSize(), sort);
        var content = result.content().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new PageImpl<>(content, pageable, result.totalElements()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<BudgetResponse> getById(@PathVariable Integer id) {
        return queryBudgets.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}/summary")
    @Operation(summary = "CU47 - Get financial summary of a budget (total, balance and payments)")
    public ResponseEntity<DtoBudgetSummary> getBudgetSummary(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(PaymentWebMapper.toDto(budgetSummaryUseCase.summary(id)));
        } catch (IllegalArgumentException e) {
            return IllegalArgumentResponses.toEmptyBody(e);
        }
    }

    @GetMapping("/person/{idPerson}")
    @Operation(summary = "Get budgets of a person (CU60)")
    public ResponseEntity<List<BudgetResponse>> getByPerson(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(queryBudgets.findByPerson(idPerson).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Status outside the vocabulary")
    })
    @GetMapping("/search")
    @Operation(summary = "Search budgets by status (CU60)")
    public ResponseEntity<List<BudgetResponse>> search(
            @Parameter(description = "Budget status (any letter case; any other value returns 400)",
                    schema = @Schema(allowableValues = {"BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "FACTURADO"}))
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(queryBudgets.findByStatus(status).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create budget")
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        return switch (manageBudgets.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created.view()));
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request (for example, status outside the vocabulary)"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update budget")
    public ResponseEntity<BudgetResponse> update(
            @PathVariable Integer id, @Valid @RequestBody BudgetRequest request) {
        return switch (manageBudgets.update(id, toCommand(request))) {
            case SaveResult.Updated updated -> ResponseEntity.ok(toResponse(updated.view()));
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageBudgets.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
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
        return ResponseEntity.ok(loadBudgetItemsFromTemplate.loadItemsFromTemplate(id, typeProcedureId).stream()
                .map(ItemResponse::from).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Budget or catalog item not found")
    })
    public record CatalogItemsRequest(@NotEmpty List<Integer> idItems) {
        /** Defensive, unmodifiable copy; {@code null} is kept so {@code @NotEmpty} reports it (#1971). */
        public CatalogItemsRequest {
            idItems = idItems == null ? null : List.copyOf(idItems);
        }
    }

    @PostMapping("/{id}/items-from-catalog")
    @Operation(summary = "CU71 - Add catalog item copies to the budget")
    public ResponseEntity<List<ItemResponse>> addItemsFromCatalog(
            @PathVariable Integer id, @Valid @RequestBody CatalogItemsRequest request) {
        return ResponseEntity.ok(addBudgetItemsFromCatalog.addItemsFromCatalog(id, request.idItems()).stream()
                .map(ItemResponse::from).toList());
    }
}
