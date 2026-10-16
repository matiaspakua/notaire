package com.licensis.notaire.adapter.in.web.item;

import java.util.List;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.application.port.in.item.ManageItemsUseCase;
import com.licensis.notaire.application.port.in.item.QueryItemsUseCase;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemCommand;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ListResult;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.SaveResult;
import com.licensis.notaire.dto.TypeItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Budget items. Calls inbound ports only; entity/BudgetRepository access live behind
 * {@code ItemOpsPort} (issue #1809).
 */
@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items", description = "API for managing budget items")
public class ItemController {

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

    private final QueryItemsUseCase queryItems;
    private final ManageItemsUseCase manageItems;

    public ItemController(QueryItemsUseCase queryItems, ManageItemsUseCase manageItems) {
        this.queryItems = queryItems;
        this.manageItems = manageItems;
    }

    private Integer resolveBudgetId(ItemRequest request) {
        if (request.budgetId() != null) {
            return request.budgetId();
        }
        return request.fkIdBudget() != null ? request.fkIdBudget().idBudget() : null;
    }

    private ItemCommand toCommand(ItemRequest request) {
        return new ItemCommand(
                request.name(),
                request.value(),
                request.percentage(),
                request.notes(),
                request.type(),
                request.reason(),
                request.fixedConcept(),
                resolveBudgetId(request));
    }

    @GetMapping
    @Operation(summary = "Get all items")
    public ResponseEntity<List<ItemResponse>> getAll() {
        return ResponseEntity.ok(queryItems.findAll().stream().map(ItemResponse::from).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<ItemResponse> getById(@PathVariable Integer id) {
        return queryItems.findById(id)
                .map(ItemResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/budget/{idBudget}")
    @Operation(summary = "Get items by budget")
    public ResponseEntity<List<ItemResponse>> getByBudget(@PathVariable Integer idBudget) {
        return ResponseEntity.ok(queryItems.findByBudget(idBudget).stream().map(ItemResponse::from).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/budget/{idBudget}/discounts-surcharges")
    @Operation(summary = "CU45/CU71 - Query discounts and surcharges of a budget")
    public ResponseEntity<List<ItemResponse>> getDiscountsAndSurcharges(@PathVariable Integer idBudget) {
        return switch (queryItems.findDiscountsAndSurchargesByBudget(idBudget)) {
            case ListResult.Ok ok -> ResponseEntity.ok(ok.items().stream().map(ItemResponse::from).toList());
            case ListResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create item")
    public ResponseEntity<Object> create(@Valid @RequestBody ItemRequest request) {
        return switch (manageItems.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(created.view()));
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update item")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody ItemRequest request) {
        return switch (manageItems.update(id, toCommand(request))) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageItems.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.Conflict ignored -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }
}
