package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoBudgetResumen;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.service.BudgetCatalogoItemsService;
import com.licensis.notaire.service.BudgetTemplateService;
import com.licensis.notaire.service.BudgetResumenService;
import com.licensis.notaire.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final BudgetService budgetService;
    private final BudgetResumenService budgetResumenService;
    private final BudgetTemplateService budgetTemplateService;
    private final BudgetCatalogoItemsService budgetCatalogoItemsService;

    public BudgetController(BudgetService budgetService,
            BudgetResumenService budgetResumenService,
            BudgetTemplateService budgetTemplateService,
            BudgetCatalogoItemsService budgetCatalogoItemsService) {
        this.budgetService = budgetService;
        this.budgetResumenService = budgetResumenService;
        this.budgetTemplateService = budgetTemplateService;
        this.budgetCatalogoItemsService = budgetCatalogoItemsService;
    }

    @GetMapping
    @Operation(summary = "Obtener presupuestos paginados",
            description = "Parámetros: page (default 0), size (default 20), sort (ej. idPresupuesto,desc)")
    public ResponseEntity<Page<Budget>> getAll(
            @PageableDefault(size = 20, sort = "idBudget", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(budgetService.findAllPaged(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener presupuesto por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Budget> getById(@PathVariable Integer id) {
        return budgetService.findById(id)
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
            return ResponseEntity.ok(budgetResumenService.getSummary(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/persona/{idPerson}")
    @Operation(summary = "Obtener presupuestos de una persona (CU60)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Budget>> getByPerson(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(budgetService.findByPerson(idPerson));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar presupuestos por estado (CU60)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Budget>> search(
            @Parameter(description = "Estado del presupuesto") @RequestParam(required = false) String status) {
        return ResponseEntity.ok(budgetService.findByStatus(status));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo presupuesto")
    public ResponseEntity<Budget> create(@RequestBody Budget entity) {
        Budget saved = budgetService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presupuesto")
    public ResponseEntity<Budget> update(@PathVariable Integer id, @RequestBody Budget entity) {
        try {
            Budget updated = budgetService.update(id, entity);
            return ResponseEntity.ok(updated);
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
    public ResponseEntity<List<Item>> agregarItemsDesdeCatalogo(
            @PathVariable Integer id,
            @RequestBody List<Integer> idItems) {
        return ResponseEntity.ok(budgetCatalogoItemsService.agregarItemsDesdeCatalogo(id, idItems));
    }
}
