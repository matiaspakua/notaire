package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.repository.ItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items", description = "API para gestionar ítems de presupuesto")
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    private final ItemRepository repository;

    public ItemController(ItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los ítems")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Item>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener ítem por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Item> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/presupuesto/{idPresupuesto}")
    @Operation(summary = "Obtener ítems por presupuesto")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Item>> getByPresupuesto(@PathVariable Integer idPresupuesto) {
        return ResponseEntity.ok(repository.findByFkIdPresupuestoIdPresupuesto(idPresupuesto));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo ítem")
    public ResponseEntity<Object> create(@RequestBody Item entity) {
        try {
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (Exception e) {
            log.error("Failed to create item", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ítem")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Item entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdItem(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
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
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete item id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
