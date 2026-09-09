package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoProcedureFolder;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.service.ProcedureFolderService;
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
public class ProcedureFolderController {

    private final ProcedureFolderService procedureFolderService;

    public ProcedureFolderController(ProcedureFolderService procedureFolderService) {
        this.procedureFolderService = procedureFolderService;
    }

    public record WaitRequest(String reason) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Carpeta no encontrada")
    })
    @GetMapping("/{id}")
    @Operation(summary = "CU85 - Consultar una carpeta de trámite por ID")
    public ResponseEntity<DtoProcedureFolder> getById(@PathVariable Integer id) {
        return procedureFolderService.findById(id)
                .map(folder -> ResponseEntity.ok(folder.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    @Operation(summary = "CU85 - Consultar carpetas de trámite por gestión o por trámite")
    public ResponseEntity<List<DtoProcedureFolder>> search(
            @RequestParam(required = false) Integer managementId,
            @RequestParam(required = false) Integer procedureId) {
        List<ProcedureFolder> carpetas;
        if (procedureId != null) {
            carpetas = procedureFolderService.findByProcedure(procedureId).map(List::of).orElse(List.of());
        } else if (managementId != null) {
            carpetas = procedureFolderService.findByManagement(managementId);
        } else {
            carpetas = List.of();
        }
        return ResponseEntity.ok(carpetas.stream().map(ProcedureFolder::getDto).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carpeta puesta en espera"),
        @ApiResponse(responseCode = "400", description = "Motivo no informado"),
        @ApiResponse(responseCode = "404", description = "Carpeta no encontrada")
    })
    @PutMapping("/{id}/espera")
    @Operation(summary = "CU85 - Poner una carpeta de trámite en espera, con motivo obligatorio")
    public ResponseEntity<Object> ponerEnWait(@PathVariable Integer id, @RequestBody WaitRequest request) {
        try {
            DtoProcedureFolder dto = procedureFolderService.ponerEnWait(id, request.reason()).getDto();
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
