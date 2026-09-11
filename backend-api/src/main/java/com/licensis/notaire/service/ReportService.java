package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.NotebookRepository;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.RegistrationDraftRepository;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.repository.TestimonyRepository;
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
public class ReportService {

    private final DataSource dataSource;
    private final TestimonyRepository testimonyRepository;
    private final NotebookRepository notebookRepository;
    private final RegistrationDraftRepository registrationDraftRepository;
    private final PaymentRepository paymentRepository;
    private final ItemRepository itemRepository;

    private static final String REPORT_PATH_BUDGET = "reportes/reportePresupuestoSinInmueble.jasper";
    private static final String REPORT_PATH_BUDGET_PROPERTIES = "reportes/reportePresupuestoInmuebles.jasper";
    private static final String REPORT_PATH_PROCEDURE_DOCUMENTS_LIST = "reportes/reporteListaDocumetosTramite.jasper";
    private static final String REPORT_PATH_MANAGEMENT_HISTORY = "reportes/reporteHistorialGestion.jasper";
    private static final String REPORT_PATH_DOCUMENTS_DUE_SOON = "reportes/reporteConsultarVencimientosDocumentos.jasper";
    private static final String REPORT_PATH_DEBT_DOCUMENTS = "reportes/reporteConsultarDeudaDocumentos.jasper";
    private static final int FOLIOS_PER_NOTEBOOK = 10;

    public ReportService(DataSource dataSource, TestimonyRepository testimonyRepository,
                           NotebookRepository notebookRepository,
                           RegistrationDraftRepository registrationDraftRepository,
                           PaymentRepository paymentRepository,
                           ItemRepository itemRepository) {
        this.dataSource = dataSource;
        this.testimonyRepository = testimonyRepository;
        this.notebookRepository = notebookRepository;
        this.registrationDraftRepository = registrationDraftRepository;
        this.paymentRepository = paymentRepository;
        this.itemRepository = itemRepository;
    }

    public byte[] generateBudgetReport(Integer idBudget) throws Exception {
        Map<String, Object> parameters = Map.of("pIdPresupuesto", idBudget);
        return generatePdfFromTemplate(REPORT_PATH_BUDGET, parameters);
    }

    public byte[] generateBudgetPropertiesReport(Integer idBudget) throws Exception {
        Map<String, Object> parameters = Map.of("idPresupuestoParam", idBudget);
        return generatePdfFromTemplate(REPORT_PATH_BUDGET_PROPERTIES, parameters);
    }

    public byte[] generateProcedureDocumentsListReport(String nameTypeProcedure) throws Exception {
        Map<String, Object> parameters = Map.of("nombreTipoTramite", nameTypeProcedure);
        return generatePdfFromTemplate(REPORT_PATH_PROCEDURE_DOCUMENTS_LIST, parameters);
    }

    public byte[] generateManagementHistoryReport(Integer idManagement) throws Exception {
        Map<String, Object> parameters = Map.of("idGestion", idManagement);
        return generatePdfFromTemplate(REPORT_PATH_MANAGEMENT_HISTORY, parameters);
    }

    public byte[] generateDocumentsDueSoonReport(Integer idSubmittedDocument) throws Exception {
        Map<String, Object> parameters = Map.of("idDocumentoPresentado", idSubmittedDocument);
        return generatePdfFromTemplate(REPORT_PATH_DOCUMENTS_DUE_SOON, parameters);
    }

    public byte[] generateDebtDocumentsReport(Integer numberManagement) throws Exception {
        Map<String, Object> parameters = Map.of("numeroGestion", numberManagement);
        return generatePdfFromTemplate(REPORT_PATH_DEBT_DOCUMENTS, parameters);
    }

    public byte[] generateIndexBookReport(Integer year) throws Exception {
        return generateSimpleTextPdf(
                "Libro de Indice",
                "CU24",
                "Periodo: " + year,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generateMonthlyTaxDeclarationReport(Integer year, Integer mes) throws Exception {
        return generateSimpleTextPdf(
                "Declaracion Jurada Mensual",
                "CU25",
                "Periodo: " + mes + "/" + year,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generateTestimonyCopyReport(Integer idTestimony) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimony));

        if (!testimony.getVerified()) {
            throw new BusinessValidationException(
                    "El testimonio debe estar verificado para emitir la copia impresa");
        }

        return generateSimpleTextPdf(
                "Copia de Testimonio N° " + testimony.getNumber(),
                "CU08",
                "Testimonio verificado",
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generateNotebookCoverReport(Integer idNotebook) {
        Notebook notebook = notebookRepository.findById(idNotebook)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el cuaderno con ID: " + idNotebook));

        int folioDesde = (notebook.getNumber() - 1) * FOLIOS_PER_NOTEBOOK + 1;
        int folioHasta = notebook.getNumber() * FOLIOS_PER_NOTEBOOK;

        return generateSimpleTextPdf(
                "Carátula de Cuaderno N° " + notebook.getNumber() + "/" + notebook.getYear(),
                "CU80",
                "Registro N° " + notebook.getFkIdNotaryPerson().getNotaryRegistrationNumber()
                        + " - Folios " + folioDesde + " a " + folioHasta,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generateIncomeTaxDeclarationReport(Integer year, Integer mes) throws Exception {
        return generateSimpleTextPdf(
                "Declaracion Jurada de Rentas",
                "CU50",
                "Periodo: " + mes + "/" + year,
                "Generado: " + LocalDate.now()
        );
    }

    public byte[] generateRegistrationDraftReport(Integer idRegistrationDraft) {
        RegistrationDraft draft = registrationDraftRepository.findById(idRegistrationDraft)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la minuta de inscripción con ID: " + idRegistrationDraft));

        return generateSimpleTextPdf(
                "Minuta de Inscripción N° " + draft.getNumber(),
                "CU82",
                "Escritura N° " + draft.getFkIdDeed().getNumber() + " - Estado: " + draft.getStatus(),
                "Generado: " + LocalDate.now()
        );
    }

    @Transactional(readOnly = true)
    public byte[] generatePaymentReceiptReport(Integer idPayment) {
        Payment payment = paymentRepository.findById(idPayment)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el pago con ID: " + idPayment));

        Budget budget = payment.getBudget();
        Person client = budget != null ? budget.getFkIdPerson() : null;
        String nameClient = client != null
                ? (client.getFirstName() + " " + client.getLastName()).trim()
                : "Cliente no identificado";

        String conceptos = budget != null
                ? itemRepository.findByFkIdBudgetIdBudget(budget.getIdBudget()).stream()
                        .map(Item::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Sin conceptos detallados")
                : "Sin conceptos detallados";

        return generateReceiptPdf(nameClient, payment.getDate(), conceptos, payment.getAmount());
    }

    private byte[] generateReceiptPdf(String client, java.util.Date date, String conceptos, float amount) {
        try {
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n");
            stream.append("/F1 20 Tf\n");
            stream.append("50 760 Td\n");
            stream.append("(Recibo de Pago) Tj\n");
            stream.append("/F1 12 Tf\n");
            stream.append("0 -30 Td\n");
            stream.append("(Cliente: ").append(escapePdfText(client)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Fecha de pago: ").append(escapePdfText(String.valueOf(date))).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Concepto(s): ").append(escapePdfText(conceptos)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Total abonado: ").append(escapePdfText(String.valueOf(amount))).append(") Tj\n");
            stream.append("0 -40 Td\n");
            stream.append("(Recibo generado por backend API - CU15.) Tj\n");
            stream.append("ET\n");

            return buildPdf(stream.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el recibo de pago: " + e.getMessage(), e);
        }
    }

    private byte[] generatePdfFromTemplate(String templatePath, Map<String, Object> parameters) throws Exception {
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

    private byte[] generateSimpleTextPdf(String titulo, String cuId, String lineaPeriodo, String lineaDate) {
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
            stream.append("(").append(escapePdfText(lineaDate)).append(") Tj\n");
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
