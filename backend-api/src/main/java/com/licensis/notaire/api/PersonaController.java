package com.licensis.notaire.api;

import com.licensis.notaire.exception.PersonaDuplicadaException;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/personas")
@Tag(name = "Personas", description = "API para gestionar personas")
public class PersonaController {

    private final PersonaService personaService;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;

    public PersonaController(PersonaService personaService, TipoIdentificacionRepository tipoIdentificacionRepository) {
        this.personaService = personaService;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las personas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Persona>> getAllPersonas() {
        List<Persona> personas = personaService.findAll();
        return ResponseEntity.ok(personas);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener persona por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Persona> getPersonaById(@PathVariable Integer id) {
        return personaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nueva persona")
    public ResponseEntity<Object> createPersona(@Valid @RequestBody Persona persona) {
        try {
            if (persona.getFkIdTipoIdentificacion() == null) {
                TipoIdentificacion defaultTipo = tipoIdentificacionRepository.findById(1)
                        .orElseGet(() -> {
                            TipoIdentificacion ti = new TipoIdentificacion();
                            ti.setNombre("DNI");
                            return tipoIdentificacionRepository.save(ti);
                        });
                persona.setFkIdTipoIdentificacion(defaultTipo);
            }
            Persona saved = personaService.save(persona);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (PersonaDuplicadaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicadaBody(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar persona")
    public ResponseEntity<Object> updatePersona(@PathVariable Integer id, @Valid @RequestBody Persona persona) {
        return personaService.findById(id)
                .map(existing -> {
                    persona.setIdPersona(id);
                    persona.setVersion(existing.getVersion());
                    if (persona.getFkIdTipoIdentificacion() == null) {
                        persona.setFkIdTipoIdentificacion(existing.getFkIdTipoIdentificacion());
                    }
                    try {
                        Persona updated = personaService.save(persona);
                        return ResponseEntity.ok((Object) updated);
                    } catch (PersonaDuplicadaException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body((Object) duplicadaBody(e));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body((Object) e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> duplicadaBody(PersonaDuplicadaException e) {
        return Map.of("message", e.getMessage(), "idPersonaExistente", e.getIdPersonaExistente());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar persona")
    public ResponseEntity<Object> deletePersona(@PathVariable Integer id) {
        if (personaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            personaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Persona is still referenced (usuario, folio, presupuesto, …):
            // surface a 409 instead of a raw 500.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: la persona está referenciada por otros registros.");
        }
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
