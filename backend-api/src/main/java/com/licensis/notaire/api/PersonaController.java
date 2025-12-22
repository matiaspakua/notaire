package com.licensis.notaire.api;

import com.licensis.notaire.jpa.PersonaJpaController;
import com.licensis.notaire.negocio.Persona;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personas")
@Tag(name = "Personas", description = "API para gestionar personas")
public class PersonaController {

    private PersonaJpaController getJpaController() {
        return PersonaJpaController.getInstancia();
    }

    @GetMapping
    @Operation(summary = "Obtener todas las personas")
    public ResponseEntity<List<Persona>> getAllPersonas() {
        try {
            List<Persona> personas = getJpaController().findPersonaEntities();
            return ResponseEntity.ok(personas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener persona por ID")
    public ResponseEntity<Persona> getPersonaById(@PathVariable Integer id) {
        try {
            Persona persona = getJpaController().findPersona(id);
            if (persona != null) {
                return ResponseEntity.ok(persona);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nueva persona")
    public ResponseEntity<Integer> createPersona(@RequestBody Persona persona) {
        try {
            int id = getJpaController().create(persona);
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar persona")
    public ResponseEntity<Void> updatePersona(@PathVariable Integer id, @RequestBody Persona persona) {
        try {
            persona.setIdPersona(id);
            getJpaController().edit(persona);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar persona")
    public ResponseEntity<Void> deletePersona(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar personas por nombre y apellido")
    public ResponseEntity<List<Persona>> buscarPersonas(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido) {
        try {
            List<Persona> personas = getJpaController().findPersonaEntities();
            // Filtrar por nombre y apellido si se proporcionan
            if (nombre != null || apellido != null) {
                final String nombreFilter = nombre != null ? nombre : "";
                final String apellidoFilter = apellido != null ? apellido : "";
                java.util.List<Persona> filtered = new java.util.ArrayList<>();
                for (Persona p : personas) {
                    boolean matches = true;
                    if (nombre != null && (p.getNombre() == null || !p.getNombre().contains(nombreFilter))) {
                        matches = false;
                    }
                    if (apellido != null && (p.getApellido() == null || !p.getApellido().contains(apellidoFilter))) {
                        matches = false;
                    }
                    if (matches) {
                        filtered.add(p);
                    }
                }
                personas = filtered;
            }
            return ResponseEntity.ok(personas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

