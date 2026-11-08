package com.licensis.notaire.adapter.in.web.notebook;

import java.util.Date;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.licensis.notaire.adapter.in.web.folio.FolioController.FolioResponse;
import com.licensis.notaire.application.port.in.notebook.ManageAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.in.notebook.QueryAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.DeedView;
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
    private final AuxiliaryProtocolStartDeedWebMapper startDeedWebMapper;

    public AuxiliaryProtocolController(
            QueryAuxiliaryProtocolUseCase queryAuxiliaryProtocol,
            ManageAuxiliaryProtocolUseCase manageAuxiliaryProtocol,
            AuxiliaryProtocolStartDeedWebMapper startDeedWebMapper) {
        this.queryAuxiliaryProtocol = queryAuxiliaryProtocol;
        this.manageAuxiliaryProtocol = manageAuxiliaryProtocol;
        this.startDeedWebMapper = startDeedWebMapper;
    }

    public record StartDeedRequest(@NotNull Integer idFolio, String body) {}

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
    public ResponseEntity<DeedResponse> startDeed(@Valid @RequestBody StartDeedRequest request) {
        return startDeedWebMapper.map(
                manageAuxiliaryProtocol.startDeed(request.idFolio(), request.body(), new Date()));
    }
}
