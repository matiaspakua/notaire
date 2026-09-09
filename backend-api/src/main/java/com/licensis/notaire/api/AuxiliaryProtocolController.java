package com.licensis.notaire.api;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.service.AuxiliaryProtocolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * REST API para la gestión de trámites en Protocolo Auxiliar (CU81).
 */
@RestController
@RequestMapping("/api/v1/protocolo-auxiliar")
@Tag(name = "Protocolo Auxiliar", description = "API para gestionar trámites en Protocolo Auxiliar")
public class AuxiliaryProtocolController {

    private final AuxiliaryProtocolService protocoloAuxiliaryService;

    public AuxiliaryProtocolController(AuxiliaryProtocolService protocoloAuxiliaryService) {
        this.protocoloAuxiliaryService = protocoloAuxiliaryService;
    }

    public record IniciarDeedRequest(Integer idFolio, String body) {
    }

    @GetMapping("/folios-disponibles")
    @Operation(summary = "Listar folios de Protocolo Auxiliar disponibles")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Folio>> getFoliosDisponibles() {
        return ResponseEntity.ok(protocoloAuxiliaryService.listarFoliosDisponibles());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creada"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Folio no encontrado")
    })
    @PostMapping("/escrituras")
    @Operation(summary = "Iniciar una escritura de Protocolo Auxiliar sobre un folio disponible")
    public ResponseEntity<Object> iniciarDeed(@RequestBody IniciarDeedRequest request) {
        try {
            Deed creada = protocoloAuxiliaryService.iniciarDeed(
                    request.idFolio(), request.body(), new Date());
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}
