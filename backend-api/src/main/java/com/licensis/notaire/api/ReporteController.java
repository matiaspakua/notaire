package com.licensis.notaire.api;

import com.licensis.notaire.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Reportes", description = "API para generación de reportes PDF")
public class ReporteController {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
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
    public ResponseEntity<byte[]> generarReportePresupuesto(
            @Parameter(description = "ID del presupuesto")
            @PathVariable Integer idPresupuesto) {
        return buildPdfResponse("presupuesto_" + idPresupuesto + ".pdf",
                () -> reporteService.generarReportePresupuesto(idPresupuesto));
    }

    @GetMapping(value = "/presupuesto-inmuebles/{idPresupuesto}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de presupuesto con inmuebles",
               description = "Genera un PDF con el presupuesto e información de inmuebles")
    public ResponseEntity<byte[]> generarReportePresupuestoInmuebles(
            @Parameter(description = "ID del presupuesto")
            @PathVariable Integer idPresupuesto) {
        return buildPdfResponse("presupuesto_inmuebles_" + idPresupuesto + ".pdf",
                () -> reporteService.generarReportePresupuestoInmuebles(idPresupuesto));
    }

    @GetMapping(value = "/lista-documentos-tramite", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de lista de documentos por trámite",
               description = "Genera un PDF con la lista de documentos requeridos para un tipo de trámite")
    public ResponseEntity<byte[]> generarReporteListaDocumentosTramite(
            @Parameter(description = "Nombre del tipo de trámite")
            @RequestParam String nombreTipoTramite) {
        return buildPdfResponse("lista_documentos.pdf",
                () -> reporteService.generarReporteListaDocumentosTramite(nombreTipoTramite));
    }

    @GetMapping(value = "/historial-gestion/{idGestion}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de historial de gestión",
               description = "Genera un PDF con el historial de una gestión específica")
    public ResponseEntity<byte[]> generarReporteHistorialGestion(
            @Parameter(description = "ID de la gestión")
            @PathVariable Integer idGestion) {
        return buildPdfResponse("historial_gestion_" + idGestion + ".pdf",
                () -> reporteService.generarReporteHistorialGestion(idGestion));
    }

    @GetMapping(value = "/documentos-por-vencer/{idDocumentoPresentado}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de documentos por vencer",
               description = "Genera un PDF con información de documentos próximos a vencer")
    public ResponseEntity<byte[]> generarReporteDocumentosPorVencer(
            @Parameter(description = "ID del documento presentado")
            @PathVariable Integer idDocumentoPresentado) {
        return buildPdfResponse("documentos_vencer.pdf",
                () -> reporteService.generarReporteDocumentosPorVencer(idDocumentoPresentado));
    }

    @GetMapping(value = "/consultar-deuda-documentos", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar reporte de consulta de deuda de documentos",
               description = "Genera un PDF con la consulta de deuda de documentos para una gestión")
    public ResponseEntity<byte[]> generarReporteConsultarDeudaDocumentos(
            @Parameter(description = "Número de gestión")
            @RequestParam Integer numeroGestion) {
        return buildPdfResponse("deuda_documentos_" + numeroGestion + ".pdf",
                () -> reporteService.generarReporteConsultarDeudaDocumentos(numeroGestion));
    }

    @GetMapping(value = "/libro-indice", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar libro de indice",
               description = "Endpoint base para CU24. Requiere plantilla Jasper de libro de indice")
    public ResponseEntity<byte[]> generarLibroIndice(
            @Parameter(description = "Año del libro de indice")
            @RequestParam Integer anio) {
        return buildPdfResponse("libro_indice_" + anio + ".pdf",
                () -> reporteService.generarReporteLibroIndice(anio));
    }

    @GetMapping(value = "/declaracion-jurada-mensual", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar declaracion jurada mensual",
               description = "Endpoint base para CU25. Requiere plantilla Jasper de DDJJ mensual")
    public ResponseEntity<byte[]> generarDeclaracionJuradaMensual(
            @Parameter(description = "Año del periodo")
            @RequestParam Integer anio,
            @Parameter(description = "Mes del periodo")
            @RequestParam Integer mes) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_mensual_" + anio + "_" + mes + ".pdf",
                () -> reporteService.generarReporteDeclaracionJuradaMensual(anio, mes));
    }

    @GetMapping(value = "/declaracion-jurada-rentas", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar declaracion jurada de rentas",
               description = "Endpoint base para CU50. Requiere plantilla Jasper de DDJJ rentas")
    public ResponseEntity<byte[]> generarDeclaracionJuradaRentas(
            @Parameter(description = "Año del periodo")
            @RequestParam Integer anio,
            @Parameter(description = "Mes del periodo")
            @RequestParam Integer mes) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().build();
        }
        return buildPdfResponse("ddjj_rentas_" + anio + "_" + mes + ".pdf",
                () -> reporteService.generarReporteDeclaracionJuradaRentas(anio, mes));
    }
}
