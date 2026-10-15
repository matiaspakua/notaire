package com.licensis.notaire.adapter.in.web.document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.licensis.notaire.application.port.in.document.ManageSubmittedDocumentsUseCase;
import com.licensis.notaire.application.port.in.document.ManageSubmittedDocumentsUseCase.SaveSubmittedDocumentCommand;
import com.licensis.notaire.application.port.in.document.QuerySubmittedDocumentsUseCase;
import com.licensis.notaire.domain.document.SubmittedDocument;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Submitted documents. Calls inbound ports only; due inheritance and persistence live in the use cases (issue #1726).
 * Create/update try/catch keep the legacy persistence-failure mapping until ad-hoc catches are removed (#579).
 */
@RestController
@RequestMapping("/api/v1/submitted-documents")
@Tag(name = "SubmittedDocument", description = "API for managing submitted documents")
public class SubmittedDocumentController {

    private static final Logger log = LoggerFactory.getLogger(SubmittedDocumentController.class);
    /** Strict ISO day; thread-safe, unlike the shared SimpleDateFormat it replaces (issue #655). */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    record TypeDocInfo(Integer idDocumentType, String name) {}

    record SubmittedDocumentResponse(
            Integer idSubmittedDocument,
            TypeDocInfo type,
            String date,
            Boolean delivered,
            Integer procedureId) {}

    record SubmittedDocumentRequest(
            @Schema(description = "Id of an existing document type; the response is 404 when it does not exist")
                    Integer typeId,
            @Schema(
                            description = "Entry date: a real day in yyyy-MM-dd format (for example 2026-09-05); "
                                    + "any other value returns 400")
                    String date,
            Boolean delivered,
            @Schema(description = "Id of an existing procedure; the response is 404 when it does not exist")
                    Integer procedureId,
            String deliveredBy,
            String name) {}

    /**
     * POST body. A submitted document is presented for a procedure of a management (CU04 / CU72):
     * without the procedure it never reaches the case summary, the documentation-complete check,
     * the CU09 debt report, the CU42 expiry list with its management, or CU10 external-entity
     * movements; without a type it has no name, expiry or delivered-by. Both are required on
     * create (issue #655). PUT keeps {@link SubmittedDocumentRequest}, where every field is
     * optional, so rows stored before this rule can still be edited.
     */
    record SubmittedDocumentCreateRequest(
            @NotNull
                    @Schema(
                            description = "Id of an existing document type; the response is 404 when it does not exist",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer typeId,
            @Schema(
                            description = "Entry date: a real day in yyyy-MM-dd format (for example 2026-09-05); "
                                    + "any other value returns 400")
                    String date,
            Boolean delivered,
            @NotNull
                    @Schema(
                            description = "Id of the management procedure this document is submitted for; "
                                    + "the response is 404 when it does not exist",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer procedureId,
            String deliveredBy,
            String name) {

        SubmittedDocumentRequest toRequest() {
            return new SubmittedDocumentRequest(typeId, date, delivered, procedureId, deliveredBy, name);
        }
    }

    private final QuerySubmittedDocumentsUseCase query;
    private final ManageSubmittedDocumentsUseCase manage;

    public SubmittedDocumentController(QuerySubmittedDocumentsUseCase query, ManageSubmittedDocumentsUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BusinessValidationException(
                    "The date must be a valid day in yyyy-MM-dd format: '" + value + "'");
        }
    }

    private SaveSubmittedDocumentCommand toCreateCommand(SubmittedDocumentRequest request) {
        return new SaveSubmittedDocumentCommand(
                request.typeId(),
                request.date() == null ? null : parseDate(request.date()),
                request.delivered(),
                request.procedureId(),
                request.deliveredBy(),
                request.name(),
                true);
    }

    private SaveSubmittedDocumentCommand toUpdateCommand(SubmittedDocumentRequest request) {
        boolean refreshDue =
                request.typeId() != null || request.date() != null || request.deliveredBy() != null;
        return new SaveSubmittedDocumentCommand(
                request.typeId(),
                request.date() == null ? null : parseDate(request.date()),
                request.delivered(),
                request.procedureId(),
                request.deliveredBy(),
                request.name(),
                refreshDue);
    }

    private SubmittedDocumentResponse toResponse(SubmittedDocument document) {
        TypeDocInfo type = null;
        if (document.documentTypeId() != null) {
            type = new TypeDocInfo(document.documentTypeId(), document.documentTypeName());
        }
        String date = document.dateEntry() != null ? DATE_FORMAT.format(document.dateEntry()) : null;
        return new SubmittedDocumentResponse(
                document.id(), type, date, document.delivered(), document.procedureId());
    }

    @GetMapping
    @Operation(summary = "Get all submitted documents")
    public ResponseEntity<List<SubmittedDocumentResponse>> getAll() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get submitted document by ID")
    public ResponseEntity<SubmittedDocumentResponse> getById(@PathVariable Integer id) {
        return query.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request (typeId or procedureId is missing, or the date is not a yyyy-MM-dd day)"),
        @ApiResponse(responseCode = "404", description = "Document type or procedure not found"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create submitted document")
    public ResponseEntity<Object> create(@Valid @RequestBody SubmittedDocumentCreateRequest body) {
        try {
            SubmittedDocument created = manage.create(toCreateCommand(body.toRequest()));
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
        } catch (ResourceNotFoundException | BusinessValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create submitted document", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request (date is not a yyyy-MM-dd day)"),
        @ApiResponse(responseCode = "404", description = "Document, document type or procedure not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update submitted document")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody SubmittedDocumentRequest request) {
        if (query.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            manage.update(id, toUpdateCommand(request));
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException | BusinessValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update submitted document id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete submitted document")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            if (!manage.delete(id)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete submitted document id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
