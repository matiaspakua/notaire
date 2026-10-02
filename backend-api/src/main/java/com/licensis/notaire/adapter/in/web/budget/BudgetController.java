package com.licensis.notaire.adapter.in.web.budget;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.adapter.in.web.payment.PaymentWebMapper;
import com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase;
import com.licensis.notaire.dto.DtoBudgetResumen;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.application.usecase.budget.BudgetCatalogItemsService;
import com.licensis.notaire.application.usecase.budget.BudgetTemplateService;
import com.licensis.notaire.application.usecase.budget.BudgetService;
import com.licensis.notaire.repository.PersonRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/presupuestos")
@Tag(name = "Presupuestos", description = "API para gestionar presupuestos")
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
            String encabezado,
            @NotBlank String status,
            @JsonAlias("amount") java.math.BigDecimal propertyAmount,
            String notes,
            Integer personId,
            @JsonProperty("person") PersonRef person) {}

    // NON_NULL so omitted client association is absent (not "person": null) — matches CU01 /
    // frontend Presupuesto.person?: DtoPerson and BudgetPersonAssociationPgIntegrationTest.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record BudgetResponse(
            Integer idBudget,
            int number,
            Date date,
            String encabezado,
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
                budget.getEncabezado(),
                budget.getStatus(),
                budget.getPropertyAmount(),
                budget.getNotes(),
                person,
                budget.getVersion());
    }

    private void applyRequest(Budget budget, BudgetRequest request) {
        if (request.number() != null) {
            budget.setNumber(request.number());
        }
        if (request.date() != null) {
            budget.setDate(request.date());
        }
        budget.setEncabezado(request.encabezado());
        budget.setStatus(request.status());
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
    @Operation(summary = "Obtener presupuestos paginados",
            description = "Parámetros: page (default 0), size (default 20), sort (ej. idPresupuesto,desc)")
    public ResponseEntity<Page<BudgetResponse>> getAll(
            @PageableDefault(size = 20, sort = "idBudget", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(budgetService.findAllPaged(pageable).map(this::toResponse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener presupuesto por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<BudgetResponse> getById(@PathVariable Integer id) {
        return budgetService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}/resumen")
    @Operation(summary = "CU47 - Obtener resumen financiero de un presupuesto (total, saldo y pagos)")
    public ResponseEntity<DtoBudgetResumen> getResumen(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(PaymentWebMapper.toDto(budgetSummaryUseCase.summary(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/persona/{idPerson}")
    @Operation(summary = "Obtener presupuestos de una persona (CU60)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BudgetResponse>> getByPerson(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(budgetService.findByPerson(idPerson).stream().map(this::toResponse).toList());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar presupuestos por estado (CU60)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BudgetResponse>> search(
            @Parameter(description = "Estado del presupuesto") @RequestParam(required = false) String status) {
        return ResponseEntity.ok(budgetService.findByStatus(status).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo presupuesto")
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        Budget entity = new Budget();
        applyRequest(entity, request);
        Budget saved = budgetService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presupuesto")
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
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presupuesto")
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
    @ApiResponse(responseCode = "400", description = "El tipo de trámite no tiene plantilla configurada"),
    @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
})
    @PostMapping("/{id}/items-desde-plantilla")
    @Operation(summary = "CU39 - Cargar ítems del presupuesto desde la plantilla del tipo de trámite")
    public ResponseEntity<List<Item>> cargarItemsDesdeTemplate(
            @PathVariable Integer id,
            @Parameter(description = "ID del tipo de trámite")
            @RequestParam("tipoTramiteId") Integer typeProcedureId) {
        return ResponseEntity.ok(budgetTemplateService.cargarItemsDesdeTemplate(id, typeProcedureId));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Presupuesto o ítem de catálogo no encontrado")
})
    @PostMapping("/{id}/items-desde-catalogo")
    @Operation(summary = "CU71 - Agregar al presupuesto copias de ítems existentes del catálogo")
    public ResponseEntity<List<Item>> addItemsFromCatalog(
            @PathVariable Integer id,
            @RequestBody List<Integer> idItems) {
        return ResponseEntity.ok(budgetCatalogoItemsService.addItemsFromCatalog(id, idItems));
    }
}
