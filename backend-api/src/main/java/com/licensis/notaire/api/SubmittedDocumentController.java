package com.licensis.notaire.api;

import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import io.swagger.v3.oas.annotations.Operation;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/documento-presentado")
@Tag(name = "DocumentoPresentado", description = "API para gestionar documentos presentados")
public class SubmittedDocumentController {

    private static final Logger log = LoggerFactory.getLogger(SubmittedDocumentController.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    record TypeDocInfo(Integer idDocumentType, String name) {}

    record SubmittedDocumentResponse(
            Integer idSubmittedDocument,
            TypeDocInfo type,
            String date,
            Boolean delivered
    ) {}

    record SubmittedDocumentRequest(Integer typeId, String date, Boolean delivered, Integer procedureId,
            String deliveredBy, String name) {}

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

    private SubmittedDocument toEntity(SubmittedDocumentRequest request) {
        SubmittedDocument entity = new SubmittedDocument();
        entity.setFkIdDocumentType(request.typeId());
        entity.setDelivered(request.delivered() != null ? request.delivered() : false);
        entity.setName(request.name() != null ? request.name() : "");
        entity.setPrepared(false);
        entity.setReleased(false);
        entity.setFlagged(false);
        entity.setReentered(false);
        if (request.procedureId() != null) {
            Procedure procedure = procedureRepository.findById(request.procedureId()).orElse(null);
            entity.setFkIdProcedure(procedure);
        }
        if (request.date() != null) {
            try {
                entity.setDateEntry(DATE_FORMAT.parse(request.date()));
            } catch (ParseException e) {
                log.warn("Invalid fecha format: {}", request.date());
            }
        }
        applyDueFromDocumentType(entity, request);
        return entity;
    }

    private void applyDueFromDocumentType(SubmittedDocument entity, SubmittedDocumentRequest request) {
        Optional<DocumentType> type = request.typeId() != null
                ? typeRepository.findById(request.typeId())
                : Optional.empty();

        boolean expires = type.map(DocumentType::getExpires).orElse(false);
        Integer dueDays = type.map(DocumentType::getDueDays).orElse(null);
        String deliveredBy = request.deliveredBy() != null
                ? request.deliveredBy()
                : type.map(DocumentType::getDeliveredBy).orElse("");

        entity.setExpires(expires);
        entity.setDueDays(dueDays);
        entity.setDeliveredBy(deliveredBy);

        if (expires && dueDays != null && entity.getDateEntry() != null) {
            Date dateDue = Date.from(
                    entity.getDateEntry().toInstant().plus(dueDays, ChronoUnit.DAYS));
            entity.setDateDue(dateDue);
        }
    }

    private SubmittedDocumentResponse toResponse(SubmittedDocument d) {
        TypeDocInfo type = null;
        Integer typeId = d.getFkIdDocumentTypeNullable();
        if (typeId != null) {
            type = typeRepository.findById(typeId)
                    .map(t -> new TypeDocInfo(t.getIdDocumentType(), t.getName()))
                    .orElse(null);
        }
        String date = d.getDateEntry() != null ? DATE_FORMAT.format(d.getDateEntry()) : null;
        return new SubmittedDocumentResponse(d.getIdSubmittedDocument(), type, date, d.getDelivered());
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
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo documento presentado")
    public ResponseEntity<Object> create(@RequestBody SubmittedDocumentRequest request) {
        try {
            SubmittedDocument entity = toEntity(request);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entity));
        } catch (Exception e) {
            log.error("Failed to create documento presentado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar documento presentado")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody SubmittedDocumentRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            SubmittedDocument entity = toEntity(request);
            entity.setIdSubmittedDocument(id);
            repository.save(entity);
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
