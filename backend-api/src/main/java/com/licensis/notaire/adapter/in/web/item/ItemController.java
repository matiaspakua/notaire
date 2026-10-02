package com.licensis.notaire.adapter.in.web.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.application.usecase.item.ItemService;
import com.licensis.notaire.repository.BudgetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items", description = "API para gestionar ítems de presupuesto")
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    record BudgetRef(@JsonProperty("idBudget") Integer idBudget) {}

    record ItemRequest(
            @NotBlank String name,
            java.math.BigDecimal value,
            Integer percentage,
            String notes,
            TypeItem type,
            String reason,
            Boolean fixedConcept,
            Integer budgetId,
            @JsonProperty("fkIdBudget") BudgetRef fkIdBudget) {}

    record ItemResponse(
            Integer idItem,
            String name,
            java.math.BigDecimal value,
            Integer percentage,
            String notes,
            TypeItem type,
            String reason,
            boolean fixedConcept,
            Integer budgetId,
            int version) {}

    private final ItemService itemService;
    private final BudgetRepository budgetRepository;

    public ItemController(ItemService itemService, BudgetRepository budgetRepository) {
        this.itemService = itemService;
        this.budgetRepository = budgetRepository;
    }

    private Integer resolveBudgetId(ItemRequest request) {
        if (request.budgetId() != null) {
            return request.budgetId();
        }
        return request.fkIdBudget() != null ? request.fkIdBudget().idBudget() : null;
    }

    private ItemResponse toResponse(Item item) {
        Integer budgetId = item.getFkIdBudget() != null ? item.getFkIdBudget().getIdBudget() : null;
        return new ItemResponse(
                item.getIdItem(),
                item.getName(),
                item.getValue(),
                item.getPercentage(),
                item.getNotes(),
                item.getType(),
                item.getReason(),
                item.isFixed(),
                budgetId,
                item.getVersion());
    }

    private void applyRequest(Item item, ItemRequest request) {
        item.setName(request.name());
        item.setValue(request.value());
        item.setPercentage(request.percentage());
        item.setNotes(request.notes());
        if (request.type() != null) {
            item.setType(request.type());
        }
        item.setReason(request.reason());
        if (request.fixedConcept() != null) {
            item.setFixedConcept(request.fixedConcept());
        }
        Integer budgetId = resolveBudgetId(request);
        if (budgetId != null) {
            Budget budget = budgetRepository.findById(budgetId).orElse(null);
            item.setFkIdBudget(budget);
        }
    }

    @GetMapping
    @Operation(summary = "Obtener todos los ítems")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ItemResponse>> getAll() {
        return ResponseEntity.ok(itemService.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener ítem por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<ItemResponse> getById(@PathVariable Integer id) {
        return itemService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/presupuesto/{idBudget}")
    @Operation(summary = "Obtener ítems por presupuesto")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ItemResponse>> getByBudget(@PathVariable Integer idBudget) {
        return ResponseEntity.ok(itemService.findByBudget(idBudget).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/presupuesto/{idBudget}/descuentos-recargos")
    @Operation(summary = "CU45/CU71 - Consultar descuentos y recargos de un presupuesto")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ItemResponse>> getDescuentosYRecargos(@PathVariable Integer idBudget) {
        try {
            return ResponseEntity.ok(itemService.findDiscountsAndSurchargesByBudget(idBudget)
                    .stream().map(this::toResponse).toList());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo ítem")
    public ResponseEntity<Object> create(@Valid @RequestBody ItemRequest request) {
        try {
            Item entity = new Item();
            applyRequest(entity, request);
            entity = itemService.create(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entity));
        } catch (BusinessValidationException e) {
            log.warn("Error de validación al crear item: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to create item", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ítem")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody ItemRequest request) {
        try {
            Item entity = itemService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado: " + id));
            applyRequest(entity, request);
            itemService.update(id, entity);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            log.warn("Error de validación al actualizar item id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update item id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ítem")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            itemService.delete(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete item id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
