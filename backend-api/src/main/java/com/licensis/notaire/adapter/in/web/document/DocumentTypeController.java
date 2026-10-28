package com.licensis.notaire.adapter.in.web.document;

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
import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase;
import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase.SaveDocumentTypeCommand;
import com.licensis.notaire.application.port.in.document.QueryDocumentTypesUseCase;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.DocumentTypeException;
import com.licensis.notaire.dto.DtoDocumentType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Document type catalog. Calls the inbound ports and maps to the wire DTO; the rules live in the use cases and the
 * domain exceptions are mapped by {@link DocumentTypeExceptionHandler} (slice 4 of the hexagon, issue #1705).
 *
 * <p>Wire defaults for create ({@code deliveredBy}, {@code enabled}, {@code returned}) stay here so OpenAPI and the
 * legacy clients keep the same behaviour. The try/catch blocks around create and update keep the persistence-failure
 * mapping of {@link ErrorResponses} until the ad-hoc catches are removed (issue #579); domain exceptions pass through.
 */
@RestController
@RequestMapping("/api/v1/document-types")
@Tag(name = "DocumentType", description = "API to manage document types")
public class DocumentTypeController {

    /** Write body for create/update (#655). */
    public record DocumentTypeWriteRequest(
            @NotBlank String name,
            Boolean expires,
            Integer dueDays,
            String deliveredBy,
            Boolean enabled,
            Boolean returned,
            Integer version) {
    }

    private final QueryDocumentTypesUseCase query;
    private final ManageDocumentTypesUseCase manage;

    public DocumentTypeController(QueryDocumentTypesUseCase query, ManageDocumentTypesUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private DtoDocumentType toDto(DocumentType type) {
        DtoDocumentType dto = new DtoDocumentType();
        dto.setIdDocumentType(type.id());
        dto.setName(type.name());
        dto.setExpires(type.expires());
        dto.setDueDays(type.dueDays());
        dto.setDeliveredBy(type.deliveredBy());
        dto.setEnabled(type.enabled());
        dto.setReturned(type.returned());
        dto.setVersion(type.version());
        return dto;
    }

    private SaveDocumentTypeCommand toCommand(DocumentTypeWriteRequest request) {
        int version = request.version() == null ? 0 : request.version();
        String deliveredBy = request.deliveredBy() == null ? "" : request.deliveredBy();
        boolean enabled = request.enabled() == null || request.enabled();
        boolean returned = request.returned() != null && request.returned();
        boolean expires = request.expires() != null && request.expires();
        return new SaveDocumentTypeCommand(
                request.name(), expires, request.dueDays(), deliveredBy, enabled, returned, version);
    }

    private SaveDocumentTypeCommand toCreateCommand(DocumentTypeWriteRequest request) {
        String deliveredBy = request.deliveredBy() == null ? "" : request.deliveredBy();
        Boolean enabled = request.enabled() == null ? true : request.enabled();
        Boolean returned = request.returned() == null ? false : request.returned();
        return toCommand(new DocumentTypeWriteRequest(
                request.name(),
                request.expires(),
                request.dueDays(),
                deliveredBy,
                enabled,
                returned,
                request.version()));
    }

    private SaveDocumentTypeCommand toUpdateCommand(DocumentTypeWriteRequest request, DocumentType existing) {
        String deliveredBy = request.deliveredBy() == null ? existing.deliveredBy() : request.deliveredBy();
        Boolean enabled = request.enabled() == null ? existing.enabled() : request.enabled();
        Boolean returned = request.returned() == null ? existing.returned() : request.returned();
        Boolean expires = request.expires() == null ? existing.expires() : request.expires();
        return toCommand(new DocumentTypeWriteRequest(
                request.name(),
                expires,
                request.dueDays(),
                deliveredBy,
                enabled,
                returned,
                request.version()));
    }

    @GetMapping
    @Operation(summary = "List all document types")
    public ResponseEntity<List<DtoDocumentType>> getAll() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toDto).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search document types by name")
    public ResponseEntity<List<DtoDocumentType>> search(@RequestParam String name) {
        return ResponseEntity.ok(query.searchByName(name).stream().map(this::toDto).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get document type by ID")
    public ResponseEntity<DtoDocumentType> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toDto(query.getById(id)));
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether the document type is in use")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        return ResponseEntity.ok(Map.of("inUse", query.isInUse(id)));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create a new document type")
    public ResponseEntity<Object> create(@Valid @RequestBody DocumentTypeWriteRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(toDto(manage.create(toCreateCommand(request))));
        } catch (DocumentTypeException domainFailure) {
            throw domainFailure;
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update document type")
    public ResponseEntity<Object> update(
            @PathVariable Integer id, @Valid @RequestBody DocumentTypeWriteRequest request) {
        try {
            DocumentType existing = query.getById(id);
            manage.update(id, toUpdateCommand(request, existing));
            return ResponseEntity.ok().build();
        } catch (DocumentTypeException domainFailure) {
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
    @Operation(summary = "Delete document type")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        manage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
