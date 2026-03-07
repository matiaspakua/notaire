package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personas")
@Tag(name = "Personas", description = "API para gestionar personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las personas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Persona>> getAllPersonas() {
        List<Persona> personas = personaService.findAll();
        return ResponseEntity.ok(personas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener persona por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Persona> getPersonaById(@PathVariable Integer id) {
        return personaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva persona")
    public ResponseEntity<Persona> createPersona(@RequestBody Persona persona) {
        Persona saved = personaService.save(persona);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar persona")
    public ResponseEntity<Persona> updatePersona(@PathVariable Integer id, @RequestBody Persona persona) {
        return personaService.findById(id)
                .map(existing -> {
                    persona.setIdPersona(id);
                    persona.setVersion(existing.getVersion());
                    Persona updated = personaService.save(persona);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar persona")
    public ResponseEntity<Void> deletePersona(@PathVariable Integer id) {
        if (personaService.findById(id).isPresent()) {
            personaService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar personas por nombre, apellido, numero de identificacion o tipo (CU61)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Persona>> buscarPersonas(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String numeroIdentificacion,
            @RequestParam(required = false) Integer idTipoIdentificacion,
            @RequestParam(required = false) Boolean esCliente) {
        
        List<Persona> personas = personaService.buscar(nombre, apellido, numeroIdentificacion, idTipoIdentificacion, esCliente);
        return ResponseEntity.ok(personas);
    }
}
