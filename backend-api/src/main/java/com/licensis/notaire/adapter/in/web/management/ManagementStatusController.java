package com.licensis.notaire.adapter.in.web.management;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase.SaveManagementStatusCommand;
import com.licensis.notaire.application.port.in.management.QueryManagementStatusesUseCase;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementStatusException;
import com.licensis.notaire.dto.DtoManagementStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Management status catalog. Calls the inbound ports and maps to the wire DTO; the rules live in the use cases and
 * the domain exceptions are mapped by {@link ManagementStatusExceptionHandler} (slice 5 of the hexagon, issue #1719).
 *
 * <p>The try/catch blocks around create and update keep the persistence-failure mapping of {@link ErrorResponses}
 * until the ad-hoc catches are removed (issue #579); domain exceptions pass through.
 */
@RestController
@RequestMapping("/api/v1/management-statuses")
@Tag(name = "Management status", description = "API for management")
public class ManagementStatusController {

    public record ManagementStatusWriteRequest(@NotBlank String name, String notes, Integer version) {}

    private final QueryManagementStatusesUseCase query;
    private final ManageManagementStatusesUseCase manage;

    public ManagementStatusController(QueryManagementStatusesUseCase query, ManageManagementStatusesUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private DtoManagementStatus toDto(ManagementStatus status) {
        DtoManagementStatus dto = new DtoManagementStatus();
        dto.setIdManagementStatus(status.id());
        dto.setName(status.name());
        dto.setNotes(status.notes());
        dto.setVersion(status.version());
        return dto;
    }

    private SaveManagementStatusCommand toCommand(ManagementStatusWriteRequest request) {
        int version = request.version() == null ? 0 : request.version();
        return new SaveManagementStatusCommand(request.name(), request.notes(), version);
    }

    @GetMapping
    @Operation(summary = "Get all management statuses")
    public ResponseEntity<List<DtoManagementStatus>> getAll() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toDto).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get management status by ID")
    public ResponseEntity<DtoManagementStatus> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toDto(query.getById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search management statuses by name")
    public ResponseEntity<List<DtoManagementStatus>> search(@RequestParam String name) {
        return ResponseEntity.ok(query.searchByName(name).stream().map(this::toDto).toList());
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether the status is referenced")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        return ResponseEntity.ok(Map.of("inUse", query.isInUse(id)));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create management status")
    public ResponseEntity<Object> create(@Valid @RequestBody ManagementStatusWriteRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(manage.create(toCommand(request))));
        } catch (ManagementStatusException domainFailure) {
            throw domainFailure;
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update management status")
    public ResponseEntity<Object> update(
            @PathVariable Integer id, @Valid @RequestBody ManagementStatusWriteRequest request) {
        try {
            manage.update(id, toCommand(request));
            return ResponseEntity.ok().build();
        } catch (ManagementStatusException domainFailure) {
            throw domainFailure;
        } catch (Exception e) {
            return ErrorResponses.updateFailed(e);
        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete management status")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        manage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
