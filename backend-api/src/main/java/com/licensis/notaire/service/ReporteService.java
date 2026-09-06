package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Cuaderno;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.MinutaInscripcion;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.CuadernoRepository;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.MinutaInscripcionRepository;
import com.licensis.notaire.repository.PagoRepository;
import com.licensis.notaire.repository.TestimonioRepository;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ReporteService {

    private final DataSource dataSource;
    private final TestimonioRepository testimonioRepository;
    private final CuadernoRepository cuadernoRepository;
    private final MinutaInscripcionRepository minutaInscripcionRepository;
    private final PagoRepository pagoRepository;
    private final ItemRepository itemRepository;

    private static final String RUTA_REPORTE_PRESUPUESTO = "reportes/reportePresupuestoSinInmueble.jasper";
    private static final String RUTA_REPORTE_PRESUPUESTO_INMUEBLES = "reportes/reportePresupuestoInmuebles.jasper";
    private static final String RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE = "reportes/reporteListaDocumetosTramite.jasper";
    private static final String RUTA_REPORTE_HISTORIAL_GESTION = "reportes/reporteHistorialGestion.jasper";
    private static final String RUTA_REPORTE_CONSULTAR_VENCIMIENTOS_DOCUMENTOS = "reportes/reporteConsultarVencimientosDocumentos.jasper";
    private static final String RUTA_REPORTE_CONSULTAR_DEUDA_DOCUMENTOS = "reportes/reporteConsultarDeudaDocumentos.jasper";
    private static final int FOLIOS_POR_CUADERNO = 10;

    public ReporteService(DataSource dataSource, TestimonioRepository testimonioRepository,
                           CuadernoRepository cuadernoRepository,
                           MinutaInscripcionRepository minutaInscripcionRepository,
                           PagoRepository pagoRepository,
                           ItemRepository itemRepository) {
        this.dataSource = dataSource;
        this.testimonioRepository = testimonioRepository;
        this.cuadernoRepository = cuadernoRepository;
        this.minutaInscripcionRepository = minutaInscripcionRepository;
        this.pagoRepository = pagoRepository;
        this.itemRepository = itemRepository;
    }

    public byte[] generarReportePresupuesto(Integer idPresupuesto) throws Exception {
        Map<String, Object> parameters = Map.of("pIdPresupuesto", idPresupuesto);
        return generarPdfDesdeTemplate(RUTA_REPORTE_PRESUPUESTO, parameters);
    }

    public byte[] generarReportePresupuestoInmuebles(Integer idPresupuesto) throws Exception {
        Map<String, Object> parameters = Map.of("idPresupuestoParam", idPresupuesto);
        return generarPdfDesdeTemplate(RUTA_REPORTE_PRESUPUESTO_INMUEBLES, parameters);
    }

    public byte[] generarReporteListaDocumentosTramite(String nombreTipoTramite) throws Exception {
        Map<String, Object> parameters = Map.of("nombreTipoTramite", nombreTipoTramite);
        return generarPdfDesdeTemplate(RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE, parameters);
    }

    public byte[] generarReporteHistorialGestion(Integer idGestion) throws Exception {
        Map<String, Object> parameters = Map.of("idGestion", idGestion);
        return generarPdfDesdeTemplate(RUTA_REPORTE_HISTORIAL_GESTION, parameters);
    }

    public byte[] generarReporteDocumentosPorVencer(Integer idDocumentoPresentado) throws Exception {
        Map<String, Object> parameters = Map.of("idDocumentoPresentado", idDocumentoPresentado);
        return generarPdfDesdeTemplate(RUTA_REPORTE_CONSULTAR_VENCIMIENTOS_DOCUMENTOS, parameters);
    }

    public byte[] generarReporteConsultarDeudaDocumentos(Integer numeroGestion) throws Exception {
        Map<String, Object> parameters = Map.of("numeroGestion", numeroGestion);
        return generarPdfDesdeTemplate(RUTA_REPORTE_CONSULTAR_DEUDA_DOCUMENTOS, parameters);
    }

    public byte[] generarReporteLibroIndice(Integer anio) throws Exception {
        return generarPdfTextoSimple(
                "Libro de Indice",
                "CU24",
                "Periodo: " + anio,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generarReporteDeclaracionJuradaMensual(Integer anio, Integer mes) throws Exception {
        return generarPdfTextoSimple(
                "Declaracion Jurada Mensual",
                "CU25",
                "Periodo: " + mes + "/" + anio,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generarReporteCopiaTestimonio(Integer idTestimonio) {
        Testimonio testimonio = testimonioRepository.findById(idTestimonio)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimonio));

        if (!testimonio.getVerificado()) {
            throw new BusinessValidationException(
                    "El testimonio debe estar verificado para emitir la copia impresa");
        }

        return generarPdfTextoSimple(
                "Copia de Testimonio N° " + testimonio.getNumero(),
                "CU08",
                "Testimonio verificado",
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generarReporteCaratulaCuaderno(Integer idCuaderno) {
        Cuaderno cuaderno = cuadernoRepository.findById(idCuaderno)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el cuaderno con ID: " + idCuaderno));

        int folioDesde = (cuaderno.getNumero() - 1) * FOLIOS_POR_CUADERNO + 1;
        int folioHasta = cuaderno.getNumero() * FOLIOS_POR_CUADERNO;

        return generarPdfTextoSimple(
                "Carátula de Cuaderno N° " + cuaderno.getNumero() + "/" + cuaderno.getAnio(),
                "CU80",
                "Registro N° " + cuaderno.getFkIdPersonaEscribano().getRegistroEscribano()
                        + " - Folios " + folioDesde + " a " + folioHasta,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generarReporteDeclaracionJuradaRentas(Integer anio, Integer mes) throws Exception {
        return generarPdfTextoSimple(
                "Declaracion Jurada de Rentas",
                "CU50",
                "Periodo: " + mes + "/" + anio,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generarReporteMinutaInscripcion(Integer idMinutaInscripcion) {
        MinutaInscripcion minuta = minutaInscripcionRepository.findById(idMinutaInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la minuta de inscripción con ID: " + idMinutaInscripcion));

        return generarPdfTextoSimple(
                "Minuta de Inscripción N° " + minuta.getNumero(),
                "CU82",
                "Escritura N° " + minuta.getFkIdEscritura().getNumero() + " - Estado: " + minuta.getEstado(),
                "Generado: " + LocalDate.now()
        );
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteReciboPago(Integer idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el pago con ID: " + idPago));

        Presupuesto presupuesto = pago.getPresupuesto();
        Persona cliente = presupuesto != null ? presupuesto.getFkIdPersona() : null;
        String nombreCliente = cliente != null
                ? (cliente.getNombre() + " " + cliente.getApellido()).trim()
                : "Cliente no identificado";

        String conceptos = presupuesto != null
                ? itemRepository.findByFkIdPresupuestoIdPresupuesto(presupuesto.getIdPresupuesto()).stream()
                        .map(Item::getNombre)
                        .filter(nombre -> nombre != null && !nombre.isBlank())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Sin conceptos detallados")
                : "Sin conceptos detallados";

        return generarPdfRecibo(nombreCliente, pago.getFecha(), conceptos, pago.getMonto());
    }

    private byte[] generarPdfRecibo(String cliente, java.util.Date fecha, String conceptos, float monto) {
        try {
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n");
            stream.append("/F1 20 Tf\n");
            stream.append("50 760 Td\n");
            stream.append("(Recibo de Pago) Tj\n");
            stream.append("/F1 12 Tf\n");
            stream.append("0 -30 Td\n");
            stream.append("(Cliente: ").append(escapePdfText(cliente)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Fecha de pago: ").append(escapePdfText(String.valueOf(fecha))).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Concepto(s): ").append(escapePdfText(conceptos)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Total abonado: ").append(escapePdfText(String.valueOf(monto))).append(") Tj\n");
            stream.append("0 -40 Td\n");
            stream.append("(Recibo generado por backend API - CU15.) Tj\n");
            stream.append("ET\n");

            return buildPdf(stream.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el recibo de pago: " + e.getMessage(), e);
        }
    }

    private byte[] generarPdfDesdeTemplate(String templatePath, Map<String, Object> parameters) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            InputStream reportStream = getClass().getClassLoader().getResourceAsStream(templatePath);
            if (reportStream == null) {
                throw new RuntimeException("No se encontró el template: " + templatePath);
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión a base de datos: " + e.getMessage(), e);
        } catch (JRException e) {
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    private byte[] generarPdfTextoSimple(String titulo, String cuId, String lineaPeriodo, String lineaFecha) {
        try {
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n");
            stream.append("/F1 20 Tf\n");
            stream.append("50 760 Td\n");
            stream.append("(").append(escapePdfText(titulo)).append(") Tj\n");
            stream.append("/F1 12 Tf\n");
            stream.append("0 -30 Td\n");
            stream.append("(Caso de uso: ").append(escapePdfText(cuId)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(").append(escapePdfText(lineaPeriodo)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(").append(escapePdfText(lineaFecha)).append(") Tj\n");
            stream.append("0 -40 Td\n");
            stream.append("(Reporte operativo generado por backend API.) Tj\n");
            stream.append("ET\n");

            return buildPdf(stream.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF textual: " + e.getMessage(), e);
        }
    }

    private byte[] buildPdf(String contentStream) throws Exception {
        ArrayList<Integer> xref = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        write(out, "%PDF-1.4\n");

        xref.add(out.size());
        write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        xref.add(out.size());
        write(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        xref.add(out.size());
        write(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");

        byte[] streamBytes = contentStream.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        xref.add(out.size());
        write(out, "4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        out.write(streamBytes);
        write(out, "endstream\nendobj\n");

        xref.add(out.size());
        write(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        int xrefStart = out.size();
        write(out, "xref\n0 6\n");
        write(out, "0000000000 65535 f \n");
        for (Integer offset : xref) {
            write(out, String.format("%010d 00000 n \n", offset));
        }
        write(out, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF");

        return out.toByteArray();
    }

    private void write(ByteArrayOutputStream out, String text) throws Exception {
        out.write(text.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private String escapePdfText(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
