package com.licensis.notaire.adapter.in.web.folio;

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

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase;
import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase.SaveFolioTypeCommand;
import com.licensis.notaire.application.port.in.folio.QueryFolioTypesUseCase;
import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeException;
import com.licensis.notaire.dto.DtoFolioType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Folio type catalog. Calls the inbound ports and maps to the wire DTO; the rules live in the use cases and the
 * domain exceptions are mapped by {@link FolioTypeExceptionHandler} (slice 3 of the hexagon, issue #1703).
 *
 * <p>The try/catch blocks around create and update keep the persistence-failure mapping of
 * {@link ErrorResponses} until the ad-hoc catches are removed (issue #579); domain exceptions pass through.
 */
@RestController
@RequestMapping("/api/v1/folio-types")
@Tag(name = "Folio type", description = "API for folio types")
public class FolioTypeController {

    private final QueryFolioTypesUseCase query;
    private final ManageFolioTypesUseCase manage;

    public FolioTypeController(QueryFolioTypesUseCase query, ManageFolioTypesUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private DtoFolioType toDto(FolioType type) {
        DtoFolioType dto = new DtoFolioType();
        dto.setIdFolioType(type.id());
        dto.setName(type.name());
        dto.setNotes(type.notes());
        dto.setEnabled(type.enabled());
        dto.setIsAuxiliary(type.auxiliary());
        dto.setVersion(type.version());
        return dto;
    }

    private SaveFolioTypeCommand toCommand(DtoFolioType dto) {
        int version = dto.getVersion() == null ? 0 : dto.getVersion();
        return new SaveFolioTypeCommand(dto.getName(), dto.getNotes(), dto.isEnabled(), dto.isIsAuxiliary(), version);
    }

    @GetMapping
    @Operation(summary = "Get all folio types")
    public ResponseEntity<List<DtoFolioType>> getAll() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toDto).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search folio types by name")
    public ResponseEntity<List<DtoFolioType>> search(@RequestParam String name) {
        return ResponseEntity.ok(query.searchByName(name).stream().map(this::toDto).toList());
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether a folio type is in use")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        return ResponseEntity.ok(Map.of("inUse", query.isInUse(id)));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get folio type by ID")
    public ResponseEntity<DtoFolioType> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toDto(query.getById(id)));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create folio type")
    public ResponseEntity<Object> create(@RequestBody DtoFolioType dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(manage.create(toCommand(dto))));
        } catch (FolioTypeException domainFailure) {
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
    @Operation(summary = "Update folio type")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoFolioType dto) {
        try {
            manage.update(id, toCommand(dto));
            return ResponseEntity.ok().build();
        } catch (FolioTypeException domainFailure) {
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
    @Operation(summary = "Delete folio type")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        manage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
