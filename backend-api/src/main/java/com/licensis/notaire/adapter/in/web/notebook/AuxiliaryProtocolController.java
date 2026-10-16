package com.licensis.notaire.adapter.in.web.notebook;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.adapter.in.web.folio.FolioController.FolioResponse;
import com.licensis.notaire.application.port.in.notebook.ManageAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.in.notebook.QueryAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.DeedView;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.StartResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API for procedures in the auxiliary protocol (CU81). Calls inbound ports only
 * (issue #1825).
 */
@RestController
@RequestMapping("/api/v1/auxiliary-protocol")
@Tag(name = "Auxiliary protocol", description = "API for managing procedures in the auxiliary protocol")
public class AuxiliaryProtocolController {

    private final QueryAuxiliaryProtocolUseCase queryAuxiliaryProtocol;
    private final ManageAuxiliaryProtocolUseCase manageAuxiliaryProtocol;

    public AuxiliaryProtocolController(
            QueryAuxiliaryProtocolUseCase queryAuxiliaryProtocol,
            ManageAuxiliaryProtocolUseCase manageAuxiliaryProtocol) {
        this.queryAuxiliaryProtocol = queryAuxiliaryProtocol;
        this.manageAuxiliaryProtocol = manageAuxiliaryProtocol;
    }

    public record StartDeedRequest(Integer idFolio, String body) {}

    public record DeedResponse(
            Integer idDeed,
            int number,
            String body,
            String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            int version) {
        static DeedResponse from(DeedView view) {
            return new DeedResponse(
                    view.idDeed(),
                    view.number(),
                    view.body(),
                    view.status(),
                    view.dateDeedrecording(),
                    view.dateRegistration(),
                    view.registrationEntryNumber(),
                    view.notes(),
                    view.version());
        }
    }

    @GetMapping("/available-folios")
    @Operation(summary = "List available auxiliary-protocol folios")
    public ResponseEntity<List<FolioResponse>> getAvailableFolios() {
        return ResponseEntity.ok(
                queryAuxiliaryProtocol.listAvailableFolios().stream().map(FolioResponse::from).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Folio not found")
    })
    @PostMapping("/deeds")
    @Operation(summary = "Start an auxiliary-protocol deed on an available folio")
    public ResponseEntity<Object> startDeed(@RequestBody StartDeedRequest request) {
        return switch (manageAuxiliaryProtocol.startDeed(request.idFolio(), request.body(), new Date())) {
            case StartResult.Created created ->
                ResponseEntity.status(HttpStatus.CREATED).body(DeedResponse.from(created.view()));
            case StartResult.BadRequest bad ->
                ResponseEntity.badRequest().body(Map.of("message", bad.message()));
            case StartResult.NotFound notFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", notFound.message()));
        };
    }
}
