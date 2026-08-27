package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.repository.MovimientoTestimonioRepository;
import com.licensis.notaire.service.MovimientoTestimonioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/movimiento-testimonio")
@Tag(name = "MovimientoTestimonio", description = "API para gestionar movimiento-testimonio")
public class MovimientoTestimonioController {

    private final MovimientoTestimonioRepository repository;
    private final MovimientoTestimonioService movimientoTestimonioService;

    public MovimientoTestimonioController(MovimientoTestimonioRepository repository,
            MovimientoTestimonioService movimientoTestimonioService) {
        this.repository = repository;
        this.movimientoTestimonioService = movimientoTestimonioService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los movimiento-testimonio")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoMovimientoTestimonio>> getAll() {
        List<DtoMovimientoTestimonio> result = repository.findAll().stream()
                .map(MovimientoTestimonio::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento-testimonio por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoMovimientoTestimonio> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo movimiento-testimonio")
    public ResponseEntity<Object> create(@RequestBody DtoMovimientoTestimonio dto) {
        try {
            MovimientoTestimonio entity = new MovimientoTestimonio();
            entity.setAtributos(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento-testimonio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoMovimientoTestimonio dto) {
        Optional<MovimientoTestimonio> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            MovimientoTestimonio entity = existing.get();
            dto.setIdMovimientoTestimonio(id);
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento-testimonio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el movimiento de testimonio está referenciado por otros registros.");
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "El testimonio no está verificado o ya tiene un movimiento abierto"),
    @ApiResponse(responseCode = "404", description = "Testimonio no encontrado")
})
    @PostMapping("/{idTestimonio}/ingresar-inscripcion")
    @Operation(summary = "Ingresar testimonio para inscripción",
               description = "Registra la fecha de ingreso de un testimonio verificado al Registro de la Propiedad")
    public ResponseEntity<DtoMovimientoTestimonio> ingresarInscripcion(@PathVariable Integer idTestimonio) {
        MovimientoTestimonio movimiento = movimientoTestimonioService.ingresarInscripcion(idTestimonio);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Falta el ingreso a inscripción previo"),
    @ApiResponse(responseCode = "404", description = "Testimonio no encontrado")
})
    @PostMapping("/{idTestimonio}/registrar-inscripcion")
    @Operation(summary = "Registrar inscripción del testimonio",
               description = "Marca como inscripto el movimiento abierto de un testimonio, registrando la fecha")
    public ResponseEntity<DtoMovimientoTestimonio> registrarInscripcion(@PathVariable Integer idTestimonio) {
        MovimientoTestimonio movimiento = movimientoTestimonioService.registrarInscripcion(idTestimonio);
        return ResponseEntity.ok(movimiento.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "El testimonio no está inscripto"),
    @ApiResponse(responseCode = "404", description = "Testimonio no encontrado")
})
    @PostMapping("/{idTestimonio}/retirar")
    @Operation(summary = "Retirar testimonio inscripto",
               description = "Registra la fecha de salida y el número de cartón de un testimonio inscripto")
    public ResponseEntity<DtoMovimientoTestimonio> retirar(@PathVariable Integer idTestimonio,
            @RequestBody DtoMovimientoTestimonio dto) {
        MovimientoTestimonio movimiento = movimientoTestimonioService.retirar(idTestimonio, dto.getNumeroCarton());
        return ResponseEntity.ok(movimiento.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "El testimonio no fue retirado previamente"),
    @ApiResponse(responseCode = "404", description = "Testimonio no encontrado")
})
    @PostMapping("/{idTestimonio}/reingresar")
    @Operation(summary = "Reingresar testimonio retirado",
               description = "Crea un nuevo movimiento de ingreso para un testimonio previamente retirado, sin "
                       + "alterar el movimiento anterior")
    public ResponseEntity<DtoMovimientoTestimonio> reingresar(@PathVariable Integer idTestimonio) {
        MovimientoTestimonio movimiento = movimientoTestimonioService.reingresar(idTestimonio);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento.getDto());
    }
}
