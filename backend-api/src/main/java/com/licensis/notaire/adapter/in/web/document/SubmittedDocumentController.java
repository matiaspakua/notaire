package com.licensis.notaire.adapter.in.web.document;

import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/documento-presentado")
@Tag(name = "DocumentoPresentado", description = "API para gestionar documentos presentados")
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
            Integer procedureId
    ) {}

    record SubmittedDocumentRequest(
            @Schema(description = "Id de un tipo de documento existente; si no existe la respuesta es 404")
            Integer typeId,
            @Schema(description = "Fecha de ingreso: un día real con formato yyyy-MM-dd (p. ej. 2026-09-05); "
                    + "otro valor responde 400")
            String date,
            Boolean delivered,
            @Schema(description = "Id de un trámite existente; si no existe la respuesta es 404")
            Integer procedureId,
            String deliveredBy,
            String name) {}

    /** Request references resolved and parsed before anything is changed (issue #655). */
    private record Resolved(Optional<DocumentType> type, Optional<Procedure> procedure, Optional<LocalDate> date) {}

    private final SubmittedDocumentRepository repository;
    private final DocumentTypeRepository typeRepository;
    private final ProcedureRepository procedureRepository;

    public SubmittedDocumentController(SubmittedDocumentRepository repository,
                                         DocumentTypeRepository typeRepository,
                                         ProcedureRepository procedureRepository) {
        this.repository = repository;
        this.typeRepository = typeRepository;
        this.procedureRepository = procedureRepository;
    }

    private SubmittedDocument newDocument() {
        SubmittedDocument entity = new SubmittedDocument();
        entity.setDelivered(false);
        entity.setName("");
        entity.setPrepared(false);
        entity.setReleased(false);
        entity.setFlagged(false);
        entity.setReentered(false);
        return entity;
    }

    private Resolved resolve(SubmittedDocumentRequest request) {
        Optional<DocumentType> type = Optional.ofNullable(request.typeId()).map(id -> typeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado: " + id)));
        Optional<Procedure> procedure = Optional.ofNullable(request.procedureId()).map(id -> procedureRepository
                .findById(id).orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado: " + id)));
        return new Resolved(type, procedure, Optional.ofNullable(request.date()).map(this::parseDate));
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BusinessValidationException(
                    "La fecha debe ser un día válido con formato yyyy-MM-dd: '" + value + "'");
        }
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Works for java.util.Date and the java.sql.Date Hibernate loads (whose toInstant() throws). */
    private static LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void apply(SubmittedDocument entity, SubmittedDocumentRequest request, Resolved resolved) {
        resolved.type().ifPresent(entity::setDocumentType);
        if (request.delivered() != null) {
            entity.setDelivered(request.delivered());
        }
        if (request.name() != null) {
            entity.setName(request.name());
        }
        resolved.procedure().ifPresent(entity::setFkIdProcedure);
        resolved.date().map(SubmittedDocumentController::toDate).ifPresent(entity::setDateEntry);
    }

    private void refreshDue(SubmittedDocument entity, SubmittedDocumentRequest request) {
        applyDueFromDocumentType(entity, Optional.ofNullable(entity.getDocumentType()), request);
    }

    private void applyDueFromDocumentType(SubmittedDocument entity, Optional<DocumentType> type,
            SubmittedDocumentRequest request) {
        boolean expires = type.map(DocumentType::getExpires).orElse(false);
        Integer dueDays = type.map(DocumentType::getDueDays).orElse(null);
        String deliveredBy = request.deliveredBy() != null
                ? request.deliveredBy()
                : type.map(DocumentType::getDeliveredBy).orElse("");

        entity.setExpires(expires);
        entity.setDueDays(dueDays);
        entity.setDeliveredBy(deliveredBy);

        if (expires && dueDays != null && entity.getDateEntry() != null) {
            entity.setDateDue(toDate(toLocalDate(entity.getDateEntry()).plusDays(dueDays)));
        }
    }

    private SubmittedDocumentResponse toResponse(SubmittedDocument d) {
        TypeDocInfo type = null;
        DocumentType documentType = d.getDocumentType();
        if (documentType != null) {
            type = new TypeDocInfo(documentType.getIdDocumentType(), documentType.getName());
        } else {
            Integer typeId = d.getFkIdDocumentTypeNullable();
            if (typeId != null) {
                type = typeRepository.findById(typeId)
                        .map(t -> new TypeDocInfo(t.getIdDocumentType(), t.getName()))
                        .orElse(null);
            }
        }
        String date = d.getDateEntry() != null ? DATE_FORMAT.format(toLocalDate(d.getDateEntry())) : null;
        Integer procedureId = d.getFkIdProcedure() != null ? d.getFkIdProcedure().getIdProcedure() : null;
        return new SubmittedDocumentResponse(d.getIdSubmittedDocument(), type, date, d.getDelivered(), procedureId);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los documentos presentados")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SubmittedDocumentResponse>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento presentado por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<SubmittedDocumentResponse> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida (fecha que no es un día yyyy-MM-dd)"),
    @ApiResponse(responseCode = "404", description = "Tipo de documento o trámite no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo documento presentado")
    public ResponseEntity<Object> create(@RequestBody SubmittedDocumentRequest request) {
        Resolved resolved = resolve(request);
        try {
            SubmittedDocument entity = newDocument();
            apply(entity, request, resolved);
            refreshDue(entity, request);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entity));
        } catch (Exception e) {
            log.error("Failed to create documento presentado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida (fecha que no es un día yyyy-MM-dd)"),
    @ApiResponse(responseCode = "404", description = "Documento, tipo de documento o trámite no encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar documento presentado")
    @Transactional
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody SubmittedDocumentRequest request) {
        Optional<SubmittedDocument> stored = repository.findById(id);
        if (stored.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Resolved resolved = resolve(request);
        try {
            apply(stored.get(), request, resolved);
            if (request.typeId() != null || request.date() != null || request.deliveredBy() != null) {
                refreshDue(stored.get(), request);
            }
            repository.save(stored.get());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update documento presentado id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar documento presentado")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete documento presentado id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
