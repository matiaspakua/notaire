package com.licensis.notaire.servicios;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Servicio para generación de reportes JasperReports
 * Ejecuta templates .jasper y devuelve PDF como bytes
 */
@Service
public class ReporteService {

    private final DataSource dataSource;

    // Rutas de los templates en resources
    private static final String RUTA_REPORTE_PRESUPUESTO = "reportes/reportePresupuestoSinInmueble.jasper";
    private static final String RUTA_REPORTE_PRESUPUESTO_INMUEBLES = "reportes/reportePresupuestoInmuebles.jasper";
    private static final String RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE = "reportes/reporteListaDocumetosTramite.jasper";
    private static final String RUTA_REPORTE_HISTORIAL_GESTION = "reportes/reporteHistorialGestion.jasper";
    private static final String RUTA_REPORTE_CONSULTAR_VENCIMIENTOS_DOCUMENTOS = "reportes/reporteConsultarVencimientosDocumentos.jasper";
    private static final String RUTA_REPORTE_CONSULTAR_DEUDA_DOCUMENTOS = "reportes/reporteConsultarDeudaDocumentos.jasper";

    public ReporteService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Genera un reporte de presupuesto (sin inmueble)
     */
    public byte[] generarReportePresupuesto(Integer idPresupuesto) throws Exception {
        Map<String, Object> parameters = Map.of("pIdPresupuesto", idPresupuesto);
        return generarPdfDesdeTemplate(RUTA_REPORTE_PRESUPUESTO, parameters);
    }

    /**
     * Genera un reporte de presupuesto con inmuebles
     */
    public byte[] generarReportePresupuestoInmuebles(Integer idPresupuesto) throws Exception {
        Map<String, Object> parameters = Map.of("idPresupuestoParam", idPresupuesto);
        return generarPdfDesdeTemplate(RUTA_REPORTE_PRESUPUESTO_INMUEBLES, parameters);
    }

    /**
     * Genera reporte de lista de documentos por trámite
     */
    public byte[] generarReporteListaDocumentosTramite(String nombreTipoTramite) throws Exception {
        Map<String, Object> parameters = Map.of("nombreTipoTramite", nombreTipoTramite);
        return generarPdfDesdeTemplate(RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE, parameters);
    }

    /**
     * Genera reporte de historial de gestión
     */
    public byte[] generarReporteHistorialGestion(Integer idGestion) throws Exception {
        Map<String, Object> parameters = Map.of("idGestion", idGestion);
        return generarPdfDesdeTemplate(RUTA_REPORTE_HISTORIAL_GESTION, parameters);
    }

    /**
     * Genera reporte de documentos por vencer
     */
    public byte[] generarReporteDocumentosPorVencer(Integer idDocumentoPresentado) throws Exception {
        Map<String, Object> parameters = Map.of("idDocumentoPresentado", idDocumentoPresentado);
        return generarPdfDesdeTemplate(RUTA_REPORTE_CONSULTAR_VENCIMIENTOS_DOCUMENTOS, parameters);
    }

    /**
     * Genera reporte de consulta de deuda de documentos
     */
    public byte[] generarReporteConsultarDeudaDocumentos(Integer numeroGestion) throws Exception {
        Map<String, Object> parameters = Map.of("numeroGestion", numeroGestion);
        return generarPdfDesdeTemplate(RUTA_REPORTE_CONSULTAR_DEUDA_DOCUMENTOS, parameters);
    }

    /**
     * Método genérico para generar PDF desde un template
     */
    private byte[] generarPdfDesdeTemplate(String templatePath, Map<String, Object> parameters) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Cargar el template .jasper desde resources
            InputStream reportStream = getClass().getClassLoader().getResourceAsStream(templatePath);
            if (reportStream == null) {
                throw new RuntimeException("No se encontró el template: " + templatePath);
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

            // Llenar el reporte con datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // Exportar a PDF
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión a base de datos: " + e.getMessage(), e);
        } catch (JRException e) {
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage(), e);
        }
    }
}
