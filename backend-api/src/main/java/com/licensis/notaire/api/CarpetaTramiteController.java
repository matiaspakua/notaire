package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoCarpetaTramite;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.CarpetaTramite;
import com.licensis.notaire.service.CarpetaTramiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API para administrar carpetas de trámite (CU85).
 */
@RestController
@RequestMapping("/api/v1/carpetas")
@Tag(name = "Carpetas de Trámite", description = "API para administrar carpetas de trámite")
public class CarpetaTramiteController {

    private final CarpetaTramiteService carpetaTramiteService;

    public CarpetaTramiteController(CarpetaTramiteService carpetaTramiteService) {
        this.carpetaTramiteService = carpetaTramiteService;
    }

    public record EsperaRequest(String motivo) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Carpeta no encontrada")
    })
    @GetMapping("/{id}")
    @Operation(summary = "CU85 - Consultar una carpeta de trámite por ID")
    public ResponseEntity<DtoCarpetaTramite> getById(@PathVariable Integer id) {
        return carpetaTramiteService.findById(id)
                .map(carpeta -> ResponseEntity.ok(carpeta.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    @Operation(summary = "CU85 - Consultar carpetas de trámite por gestión o por trámite")
    public ResponseEntity<List<DtoCarpetaTramite>> search(
            @RequestParam(required = false) Integer gestionId,
            @RequestParam(required = false) Integer tramiteId) {
        List<CarpetaTramite> carpetas;
        if (tramiteId != null) {
            carpetas = carpetaTramiteService.findByTramite(tramiteId).map(List::of).orElse(List.of());
        } else if (gestionId != null) {
            carpetas = carpetaTramiteService.findByGestion(gestionId);
        } else {
            carpetas = List.of();
        }
        return ResponseEntity.ok(carpetas.stream().map(CarpetaTramite::getDto).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carpeta puesta en espera"),
        @ApiResponse(responseCode = "400", description = "Motivo no informado"),
        @ApiResponse(responseCode = "404", description = "Carpeta no encontrada")
    })
    @PutMapping("/{id}/espera")
    @Operation(summary = "CU85 - Poner una carpeta de trámite en espera, con motivo obligatorio")
    public ResponseEntity<Object> ponerEnEspera(@PathVariable Integer id, @RequestBody EsperaRequest request) {
        try {
            DtoCarpetaTramite dto = carpetaTramiteService.ponerEnEspera(id, request.motivo()).getDto();
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
