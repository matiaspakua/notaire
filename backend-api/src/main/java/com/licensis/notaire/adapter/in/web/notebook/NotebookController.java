package com.licensis.notaire.adapter.in.web.notebook;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.adapter.in.web.report.PdfResponseFactory;
import com.licensis.notaire.adapter.in.web.support.NotaryRef;
import com.licensis.notaire.application.port.in.notebook.ManageNotebooksUseCase;
import com.licensis.notaire.application.port.in.notebook.QueryNotebooksUseCase;
import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort.CreateResult;
import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort.NotebookView;
import com.licensis.notaire.application.usecase.report.ReportService;
import com.licensis.notaire.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;

/**
 * Folio notebooks (CU80). CRUD calls inbound ports only; cover PDF keeps {@link ReportService}
 * (issue #1817).
 */
@RestController
@RequestMapping("/api/v1/notebooks")
@Validated
@Tag(name = "Notebooks", description = "API for managing folio notebooks")
public class NotebookController {

    private static final Logger logger = LoggerFactory.getLogger(NotebookController.class);

    private final QueryNotebooksUseCase queryNotebooks;
    private final ManageNotebooksUseCase manageNotebooks;
    private final ReportService reportService;

    public NotebookController(
            QueryNotebooksUseCase queryNotebooks,
            ManageNotebooksUseCase manageNotebooks,
            ReportService reportService) {
        this.queryNotebooks = queryNotebooks;
        this.manageNotebooks = manageNotebooks;
        this.reportService = reportService;
    }

    public record NotebookResponse(
            Integer idNotebook, int number, int year, String notes, NotaryRef fkIdNotaryPerson, int version) {
        static NotebookResponse from(NotebookView view) {
            var n = view.notary();
            NotaryRef notary = n == null
                    ? null
                    : new NotaryRef(
                            n.personId(),
                            n.firstName(),
                            n.lastName(),
                            n.identificationNumber(),
                            n.notaryRegistrationNumber());
            return new NotebookResponse(
                    view.idNotebook(), view.number(), view.year(), view.notes(), notary, view.version());
        }
    }

    public record CreateNotebookRequest(
            @NotEmpty List<Integer> idsFolio, Integer idNotary, int year, String notes) {}

    @GetMapping
    @Operation(summary = "Get all notebooks")
    public ResponseEntity<List<NotebookResponse>> getAllNotebooks() {
        return ResponseEntity.ok(queryNotebooks.findAll().stream().map(NotebookResponse::from).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get notebook by ID")
    public ResponseEntity<NotebookResponse> getNotebookById(@PathVariable Integer id) {
        return queryNotebooks
                .findById(id)
                .map(NotebookResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Notary or folio not found"),
        @ApiResponse(
                responseCode = "409",
                description = "A notebook already exists with that number for the year and notary")
    })
    @PostMapping
    @Operation(summary = "Create a notebook from ten consecutive folios")
    public ResponseEntity<Object> createNotebook(@RequestBody CreateNotebookRequest request) {
        return switch (manageNotebooks.create(
                request.idsFolio(), request.idNotary(), request.year(), request.notes())) {
            case CreateResult.Created created ->
                ResponseEntity.status(HttpStatus.CREATED).body(NotebookResponse.from(created.view()));
            case CreateResult.BadRequest bad ->
                ResponseEntity.badRequest().body(Map.of("message", bad.message()));
            case CreateResult.NotFound notFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", notFound.message()));
        };
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
            return PdfResponseFactory.ok(pdf, "notebook_" + id + "_cover.pdf");
        } catch (ResourceNotFoundException e) {
            logger.info("Cover requested for a missing notebook: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
