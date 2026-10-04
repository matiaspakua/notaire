package com.licensis.notaire.adapter.in.web.document;

import com.licensis.notaire.application.usecase.document.UpcomingExpirationService;
import com.licensis.notaire.dto.DtoUpcomingExpiration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documento-presentado")
@Tag(name = "DocumentoPresentado", description = "API para gestionar documentos presentados")
public class UpcomingExpirationController {

    private final UpcomingExpirationService upcomingExpirationService;

    public UpcomingExpirationController(UpcomingExpirationService upcomingExpirationService) {
        this.upcomingExpirationService = upcomingExpirationService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "La ventana de días está fuera de 1 a 365")
    })
    @GetMapping("/proximos-vencimientos")
    @Operation(summary = "CU42 - Informar próximos vencimientos",
               description = "Documentos presentados que vencen entre hoy y hoy más 'dias' días (por defecto 30), "
                       + "no liberados, ordenados por fecha de vencimiento")
    public ResponseEntity<List<DtoUpcomingExpiration>> getUpcoming(
            @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(upcomingExpirationService.findUpcoming(dias, LocalDate.now()));
    }
}
