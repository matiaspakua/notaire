package com.licensis.notaire.adapter.in.web.history;

import java.util.Date;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.licensis.notaire.application.port.in.history.ManageHistoriesUseCase;
import com.licensis.notaire.application.port.in.history.QueryHistoriesUseCase;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.HistoryCommand;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoHistorySummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Management history. Calls inbound ports only; orchestration and persistence live in the use cases
 * (issue #1775).
 */
@RestController
@RequestMapping("/api/v1/history")
@Tag(name = "ManagementHistory", description = "API for managing management history")
public class HistoryController {

    record HistoryRequest(
            Date date,
            String notes,
            @NotNull Integer managementStatusId,
            @NotNull Integer managementId) {}

    private final QueryHistoriesUseCase queryHistories;
    private final ManageHistoriesUseCase manageHistories;

    public HistoryController(QueryHistoriesUseCase queryHistories, ManageHistoriesUseCase manageHistories) {
        this.queryHistories = queryHistories;
        this.manageHistories = manageHistories;
    }

    @GetMapping
    @Operation(summary = "Get history, one page at a time",
            description = "Spring Data page (20 rows by default, ordered by idHistory ascending). "
                    + "Use sort=idHistory,desc to see the most recent changes first. "
                    + "The full history of a management is at GET /api/v1/history/management/{idManagement}.")
    public ResponseEntity<Page<DtoHistorySummary>> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "idHistory") Pageable pageable) {
        var order = pageable.getSort().stream().findFirst().orElse(null);
        String sort = order == null
                ? "idHistory,asc"
                : order.getProperty() + "," + order.getDirection().name().toLowerCase();
        var result = queryHistories.findAll(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return ResponseEntity.ok(new PageImpl<>(result.content(), pageable, result.totalElements()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get history by ID")
    public ResponseEntity<DtoHistorySummary> getById(@PathVariable Integer id) {
        return queryHistories.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/management/{idManagement}")
    @Operation(summary = "Get history of a management (CU13)")
    public ResponseEntity<List<DtoHistorySummary>> getByManagement(@PathVariable Integer idManagement) {
        return ResponseEntity.ok(queryHistories.findByManagementId(idManagement));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create a history record")
    public ResponseEntity<Object> create(@Valid @RequestBody HistoryRequest request) {
        return switch (manageHistories.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(created.summary());
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Only an administrator can update the history"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update history (administrator only)",
            description = "The history is the trace of status changes of the management (CU13): "
                    + "only an administrator can correct a record.")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody HistoryRequest request) {
        return switch (manageHistories.update(id, toCommand(request))) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Only an administrator can delete the history"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete history (administrator only)",
            description = "The history is the trace of status changes of the management (CU13): "
                    + "only an administrator can delete a record.")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageHistories.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.Conflict ignored -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }

    private static HistoryCommand toCommand(HistoryRequest request) {
        return new HistoryCommand(
                request.date(),
                request.notes(),
                request.managementStatusId(),
                request.managementId());
    }
}
