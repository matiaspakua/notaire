package com.licensis.notaire.adapter.in.web.copy;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.licensis.notaire.application.port.in.copy.ManageCopiesUseCase;
import com.licensis.notaire.application.port.in.copy.QueryCopiesUseCase;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyCommand;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyView;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Copies. Calls inbound ports only; orchestration and persistence live in the use cases
 * (issue #1779).
 */
@RestController
@RequestMapping("/api/v1/copies")
@Tag(name = "Copy", description = "API for managing copies")
public class CopyController {

    /**
     * Create and update body. The copy number and print date are NOT NULL columns and PUT
     * replaces them, so both are required on both operations (issue #655).
     */
    record CopyRequest(
            @NotNull @Schema(description = "Number of copy", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer number,
            @NotNull @Schema(description = "Print date", requiredMode = Schema.RequiredMode.REQUIRED)
            Date datePrinting,
            Date dateWithdrawal,
            String notes,
            Integer personId,
            Integer testimonyId) {}

    record CopyResponse(
            Integer idCopy,
            int number,
            Date datePrinting,
            Date dateWithdrawal,
            String notes,
            Integer personId,
            Integer testimonyId,
            int version) {}

    private final QueryCopiesUseCase queryCopies;
    private final ManageCopiesUseCase manageCopies;

    public CopyController(QueryCopiesUseCase queryCopies, ManageCopiesUseCase manageCopies) {
        this.queryCopies = queryCopies;
        this.manageCopies = manageCopies;
    }

    @GetMapping
    @Operation(summary = "Get all copies")
    public ResponseEntity<List<CopyResponse>> getAll() {
        return ResponseEntity.ok(queryCopies.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get copy by ID")
    public ResponseEntity<CopyResponse> getById(@PathVariable Integer id) {
        return queryCopies.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create copy")
    public ResponseEntity<Object> create(@Valid @RequestBody CopyRequest request) {
        return switch (manageCopies.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created.view()));
            case SaveResult.Conflict conflict -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", conflict.message()));
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update copy")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody CopyRequest request) {
        return switch (manageCopies.update(id, toCommand(request))) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Conflict ignored -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete copy")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageCopies.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.Conflict ignored -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }

    private CopyResponse toResponse(CopyView view) {
        return new CopyResponse(
                view.idCopy(),
                view.number(),
                view.datePrinting(),
                view.dateWithdrawal(),
                view.notes(),
                view.personId(),
                view.testimonyId(),
                view.version());
    }

    private static CopyCommand toCommand(CopyRequest request) {
        return new CopyCommand(
                request.number(),
                request.datePrinting(),
                request.dateWithdrawal(),
                request.notes(),
                request.personId(),
                request.testimonyId());
    }
}
