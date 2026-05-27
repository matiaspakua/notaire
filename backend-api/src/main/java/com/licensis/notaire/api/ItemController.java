package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.repository.ItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Item>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ítem por ID")
    public ResponseEntity<Item> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/presupuesto/{idPresupuesto}")
    @Operation(summary = "Obtener ítems por presupuesto")
    public ResponseEntity<List<Item>> getByPresupuesto(@PathVariable Integer idPresupuesto) {
        return ResponseEntity.ok(repository.findByFkIdPresupuestoIdPresupuesto(idPresupuesto));
    }

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
