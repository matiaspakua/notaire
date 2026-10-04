package com.licensis.notaire.adapter.in.web.management;

import com.licensis.notaire.application.usecase.management.ManagementCaseSummaryService;
import com.licensis.notaire.dto.DtoManagementCaseSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestiones")
@Tag(name = "Managements", description = "API for deed managements")
public class ManagementCaseSummaryController {

    private final ManagementCaseSummaryService managementCaseSummaryService;

    public ManagementCaseSummaryController(ManagementCaseSummaryService managementCaseSummaryService) {
        this.managementCaseSummaryService = managementCaseSummaryService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/resumen-caso")
    @Operation(summary = "CU07/CU11/CU12/CU70 - Get the escrituras, testimonios and copias of a management")
    public ResponseEntity<DtoManagementCaseSummary> getCaseSummary(@PathVariable Integer id) {
        return ResponseEntity.ok(managementCaseSummaryService.getSummary(id));
    }
}
