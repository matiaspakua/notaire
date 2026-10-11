package com.licensis.notaire.adapter.in.web.notebook;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.adapter.in.web.support.NotaryRef;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.application.usecase.notebook.NotebookService;
import com.licensis.notaire.application.usecase.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for managing folio notebooks (CU80).
 */
@RestController
@RequestMapping("/api/v1/notebooks")
@Validated
@Tag(name = "Notebooks", description = "API for managing folio notebooks")
public class NotebookController {

    private static final Logger logger = LoggerFactory.getLogger(NotebookController.class);

    private final NotebookService notebookService;
    private final ReportService reportService;

    public NotebookController(NotebookService notebookService, ReportService reportService) {
        this.notebookService = notebookService;
        this.reportService = reportService;
    }

    public record NotebookResponse(Integer idNotebook, int number, int year, String notes,
            NotaryRef fkIdNotaryPerson, int version) {
        public static NotebookResponse from(Notebook notebook) {
            return new NotebookResponse(notebook.getIdNotebook(), notebook.getNumber(), notebook.getYear(),
                    notebook.getNotes(), NotaryRef.from(notebook.getFkIdNotaryPerson()), notebook.getVersion());
        }
    }

    public record CreateNotebookRequest(
            @NotEmpty List<Integer> idsFolio,
            Integer idNotary,
            int year,
            String notes) {
    }

    @GetMapping
    @Operation(summary = "Get all notebooks")
    @Transactional(readOnly = true)
    public ResponseEntity<List<NotebookResponse>> getAllNotebooks() {
        return ResponseEntity.ok(notebookService.findAll().stream().map(NotebookResponse::from).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get notebook by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<NotebookResponse> getNotebookById(@PathVariable Integer id) {
        return notebookService.findById(id)
                .map(notebook -> ResponseEntity.ok(NotebookResponse.from(notebook)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Notary or folio not found"),
        @ApiResponse(responseCode = "409", description = "A notebook already exists with that number for the year and notary")
    })
    @PostMapping
    @Operation(summary = "Create a notebook from ten consecutive folios")
    public ResponseEntity<Object> createNotebook(@RequestBody CreateNotebookRequest request) {
        try {
            Notebook created = notebookService.createNotebook(
                    request.idsFolio(), request.idNotary(), request.year(), request.notes());
            return ResponseEntity.status(HttpStatus.CREATED).body(NotebookResponse.from(created));
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Notebook not found")
    })
    @GetMapping(value = "/{id}/cover", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generate the cover PDF of a notebook")
    public ResponseEntity<byte[]> getCover(@PathVariable Integer id) {
        try {
            byte[] pdf = reportService.generateNotebookCoverReport(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"notebook_" + id + "_cover.pdf\"")
                    .body(pdf);
        } catch (ResourceNotFoundException e) {
            logger.info("Cover requested for a missing notebook: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
