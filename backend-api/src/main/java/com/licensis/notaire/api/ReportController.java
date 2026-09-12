package com.licensis.notaire.api;

import com.licensis.notaire.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para generación de reportes
 * Expone endpoints para generar PDFs desde JasperReports
 */
@RestController
@RequestMapping("/api/v1/reportes")
@Validated
@Tag(name = "Reportes", description = "API para generación de reportes PDF")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reporteService;

    public ReportController(ReportService reporteService) {
        this.reporteService = reporteService;
    }

    @FunctionalInterface
    private interface ReportGenerator {
        byte[] generate() throws Exception;
    }

    private ResponseEntity<byte[]> buildPdfResponse(String filename, ReportGenerator generator) {
        try {
            byte[] pdfBytes = generator.generate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Failed to generate report '{}'", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/presupuesto/{idPresupuesto}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de presupuesto",
               description = "Genera un PDF con el presupuesto especificado")
    public ResponseEntity<byte[]> generateBudgetReport(
            @Parameter(description = "ID del presupuesto")
            @PathVariable("idPresupuesto") @Positive Integer idBudget) {
        return buildPdfResponse("presupuesto_" + idBudget + ".pdf",
                () -> reporteService.generateBudgetReport(idBudget));
    }

    @GetMapping(value = "/presupuesto-inmuebles/{idPresupuesto}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de presupuesto con inmuebles",
               description = "Genera un PDF con el presupuesto e información de inmuebles")
    public ResponseEntity<byte[]> generateBudgetPropertiesReport(
            @Parameter(description = "ID del presupuesto")
            @PathVariable("idPresupuesto") @Positive Integer idBudget) {
        return buildPdfResponse("presupuesto_inmuebles_" + idBudget + ".pdf",
                () -> reporteService.generateBudgetPropertiesReport(idBudget));
    }

    @GetMapping(value = "/lista-documentos-tramite", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de lista de documentos por trámite",
               description = "Genera un PDF con la lista de documentos requeridos para un tipo de trámite")
    public ResponseEntity<byte[]> generateProcedureDocumentsListReport(
            @Parameter(description = "Nombre del tipo de trámite")
            @RequestParam("nombreTipoTramite") @NotBlank String nameTypeProcedure) {
        return buildPdfResponse("lista_documentos.pdf",
                () -> reporteService.generateProcedureDocumentsListReport(nameTypeProcedure));
    }

    @GetMapping(value = "/historial-gestion/{idGestion}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de historial de gestión",
               description = "Genera un PDF con el historial de una gestión específica")
    public ResponseEntity<byte[]> generateManagementHistoryReport(
            @Parameter(description = "ID de la gestión")
            @PathVariable("idGestion") @Positive Integer idManagement) {
        return buildPdfResponse("historial_gestion_" + idManagement + ".pdf",
                () -> reporteService.generateManagementHistoryReport(idManagement));
    }

    @GetMapping(value = "/documentos-por-vencer/{idDocumentoPresentado}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de documentos por vencer",
               description = "Genera un PDF con información de documentos próximos a vencer")
    public ResponseEntity<byte[]> generateDocumentsDueSoonReport(
            @Parameter(description = "ID del documento presentado")
            @PathVariable("idDocumentoPresentado") @Positive Integer idSubmittedDocument) {
        return buildPdfResponse("documentos_vencer.pdf",
                () -> reporteService.generateDocumentsDueSoonReport(idSubmittedDocument));
    }

    @GetMapping(value = "/consultar-deuda-documentos", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de consulta de deuda de documentos",
               description = "Genera un PDF con la consulta de deuda de documentos para una gestión")
    public ResponseEntity<byte[]> generateDebtDocumentsReport(
            @Parameter(description = "Número de gestión")
            @RequestParam @Positive Integer numberManagement) {
        return buildPdfResponse("deuda_documentos_" + numberManagement + ".pdf",
                () -> reporteService.generateDebtDocumentsReport(numberManagement));
    }

    @GetMapping(value = "/libro-indice", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar libro de indice",
               description = "Endpoint base para CU24. Requiere plantilla Jasper de libro de indice")
    public ResponseEntity<byte[]> generarLibroIndice(
            @Parameter(description = "Año del libro de indice")
            @RequestParam("anio") @Positive Integer year) {
        return buildPdfResponse("libro_indice_" + year + ".pdf",
                () -> reporteService.generateIndexBookReport(year));
    }

    @GetMapping(value = "/declaracion-jurada-mensual", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar declaracion jurada mensual",
               description = "Endpoint base para CU25. Requiere plantilla Jasper de DDJJ mensual")
    public ResponseEntity<byte[]> generarDeclaracionJuradaMensual(
            @Parameter(description = "Año del periodo")
            @RequestParam("anio") @Positive Integer year,
            @Parameter(description = "Mes del periodo")
            @RequestParam @Min(1) @Max(12) Integer mes) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_mensual_" + year + "_" + mes + ".pdf",
                () -> reporteService.generateMonthlyTaxDeclarationReport(year, mes));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "El testimonio no está verificado"),
    @ApiResponse(responseCode = "404", description = "Testimonio no encontrado")
})
    @GetMapping(value = "/testimonio/{idTestimonio}/copia", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar copia impresa de testimonio verificado",
               description = "Genera un PDF con la copia impresa de un testimonio, solo si ya fue verificado")
    public ResponseEntity<byte[]> generateTestimonyCopyReport(
            @Parameter(description = "ID del testimonio")
            @PathVariable("idTestimonio") @Positive Integer idTestimony) {
        byte[] pdfBytes = reporteService.generateTestimonyCopyReport(idTestimony);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"testimonio_" + idTestimony + "_copia.pdf\"")
                .body(pdfBytes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Minuta de inscripción no encontrada")
    })
    @GetMapping(value = "/minuta-inscripcion/{idMinutaInscripcion}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "CU82 - Generar el formulario normalizado de la minuta de inscripción",
               description = "Genera un PDF con el formulario normalizado de una minuta de inscripción")
    public ResponseEntity<byte[]> generateRegistrationDraftReport(
            @Parameter(description = "ID de la minuta de inscripción")
            @PathVariable("idMinutaInscripcion") @Positive Integer idRegistrationDraft) {
        byte[] pdfBytes = reporteService.generateRegistrationDraftReport(idRegistrationDraft);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"minuta_inscripcion_" + idRegistrationDraft + ".pdf\"")
                .body(pdfBytes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping(value = "/recibo-pago/{idPago}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "CU15 - Emitir recibo de pago",
               description = "Genera un PDF con el recibo de un pago existente")
    public ResponseEntity<byte[]> generatePaymentReceiptReport(
            @Parameter(description = "ID del pago")
            @PathVariable("idPago") @Positive Integer idPayment) {
        byte[] pdfBytes = reporteService.generatePaymentReceiptReport(idPayment);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"recibo_pago_" + idPayment + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping(value = "/declaracion-jurada-rentas", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar declaracion jurada de rentas",
               description = "Endpoint base para CU50. Requiere plantilla Jasper de DDJJ rentas")
    public ResponseEntity<byte[]> generarDeclaracionJuradaRentas(
            @Parameter(description = "Año del periodo")
            @RequestParam("anio") @Positive Integer year,
            @Parameter(description = "Mes del periodo")
            @RequestParam @Min(1) @Max(12) Integer mes) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_rentas_" + year + "_" + mes + ".pdf",
                () -> reporteService.generateIncomeTaxDeclarationReport(year, mes));
    }
}
