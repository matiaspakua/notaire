package com.licensis.notaire.api;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Cuaderno;
import com.licensis.notaire.service.CuadernoService;
import com.licensis.notaire.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API para administrar cuadernos de folios (CU80).
 */
@RestController
@RequestMapping("/api/v1/cuadernos")
@Validated
@Tag(name = "Cuadernos", description = "API para administrar cuadernos de folios")
public class CuadernoController {

    private static final Logger logger = LoggerFactory.getLogger(CuadernoController.class);

    private final CuadernoService cuadernoService;
    private final ReporteService reporteService;

    public CuadernoController(CuadernoService cuadernoService, ReporteService reporteService) {
        this.cuadernoService = cuadernoService;
        this.reporteService = reporteService;
    }

    public record CrearCuadernoRequest(
            @NotEmpty List<Integer> idsFolio,
            Integer idEscribano,
            int anio,
            String observaciones) {
    }

    @GetMapping
    @Operation(summary = "Obtener todos los cuadernos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Cuaderno>> getAllCuadernos() {
        return ResponseEntity.ok(cuadernoService.findAll());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuaderno por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Cuaderno> getCuadernoById(@PathVariable Integer id) {
        return cuadernoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Escribano o folio no encontrado")
    })
    @PostMapping
    @Operation(summary = "Crear un nuevo cuaderno a partir de diez folios consecutivos")
    public ResponseEntity<Object> crearCuaderno(@RequestBody CrearCuadernoRequest request) {
        try {
            Cuaderno creado = cuadernoService.crearCuaderno(
                    request.idsFolio(), request.idEscribano(), request.anio(), request.observaciones());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Cuaderno no encontrado")
    })
    @GetMapping(value = "/{id}/caratula", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar la carátula PDF de un cuaderno")
    public ResponseEntity<byte[]> getCaratula(@PathVariable Integer id) {
        try {
            byte[] pdf = reporteService.generarReporteCaratulaCuaderno(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cuaderno_" + id + "_caratula.pdf\"")
                    .body(pdf);
        } catch (ResourceNotFoundException e) {
            logger.info("Carátula solicitada para cuaderno inexistente: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
