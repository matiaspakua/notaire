package com.licensis.notaire.api;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.PlantillaCostoDocumento;
import com.licensis.notaire.service.PlantillaCostoDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API para administrar la plantilla de costos de documentos por tipo de trámite (CU27/CU39).
 */
@RestController
@RequestMapping("/api/v1/plantilla-costos-documento")
@Tag(name = "PlantillaCostosDocumento", description = "API para administrar costos esperados de documentos por tipo de trámite")
public class PlantillaCostoDocumentoController {

    private final PlantillaCostoDocumentoService plantillaCostoDocumentoService;

    public PlantillaCostoDocumentoController(PlantillaCostoDocumentoService plantillaCostoDocumentoService) {
        this.plantillaCostoDocumentoService = plantillaCostoDocumentoService;
    }

    public record CrearCostoRequest(
            Integer idTipoTramite,
            Integer idTipoDocumento,
            Float montoFijo,
            Float porcentajeVariable) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Tipo de trámite o documento no encontrado")
    })
    @PostMapping
    @Operation(summary = "Definir el costo (fijo o variable) de un tipo de documento en la plantilla de un tipo de trámite")
    public ResponseEntity<Object> crearCosto(@RequestBody CrearCostoRequest request) {
        try {
            PlantillaCostoDocumento creado = plantillaCostoDocumentoService.crear(
                    request.idTipoTramite(), request.idTipoDocumento(),
                    request.montoFijo(), request.porcentajeVariable());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/tipo-tramite/{idTipoTramite}")
    @Operation(summary = "Obtener los costos de documentos definidos para un tipo de trámite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlantillaCostoDocumento>> getCostosByTipoTramite(
            @PathVariable Integer idTipoTramite) {
        return ResponseEntity.ok(plantillaCostoDocumentoService.findByTipoTramite(idTipoTramite));
    }
}
