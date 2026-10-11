package com.licensis.notaire.application.usecase.report;

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
import com.licensis.notaire.application.port.out.report.ReportRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Generates the report PDFs exposed by {@code /api/v1/reports}.
 *
 * <p>The six reports that were JasperReports 3.5.3 templates over the legacy schema
 * (budget, budget with properties, procedure documents, management history, document
 * expiry and document debt) are built from the current domain model by
 * {@link ReportDocumentFactory} and rendered through the {@link ReportRenderer} port
 * (issue #567).
 */
@Service
public class ReportService {

    private final TestimonyRepository testimonyRepository;
    private final NotebookRepository notebookRepository;
    private final RegistrationDraftRepository registrationDraftRepository;
    private final PaymentRepository paymentRepository;
    private final ItemRepository itemRepository;
    private final ReportDocumentFactory reportDocuments;
    private final ReportRenderer reportRenderer;

    private static final int FOLIOS_PER_NOTEBOOK = 10;

    public ReportService(TestimonyRepository testimonyRepository,
                           NotebookRepository notebookRepository,
                           RegistrationDraftRepository registrationDraftRepository,
                           PaymentRepository paymentRepository,
                           ItemRepository itemRepository,
                           ReportDocumentFactory reportDocuments,
                           ReportRenderer reportRenderer) {
        this.testimonyRepository = testimonyRepository;
        this.notebookRepository = notebookRepository;
        this.registrationDraftRepository = registrationDraftRepository;
        this.paymentRepository = paymentRepository;
        this.itemRepository = itemRepository;
        this.reportDocuments = reportDocuments;
        this.reportRenderer = reportRenderer;
    }

    public byte[] generateBudgetReport(Integer idBudget) {
        return reportRenderer.render(reportDocuments.budget(idBudget, false));
    }

    public byte[] generateBudgetPropertiesReport(Integer idBudget) {
        return reportRenderer.render(reportDocuments.budget(idBudget, true));
    }

    public byte[] generateProcedureDocumentsListReport(String nameTypeProcedure) {
        return reportRenderer.render(reportDocuments.procedureDocuments(nameTypeProcedure));
    }

    public byte[] generateManagementHistoryReport(Integer idManagement) {
        return reportRenderer.render(reportDocuments.managementHistory(idManagement));
    }

    public byte[] generateDocumentsDueSoonReport(Integer idSubmittedDocument) {
        return reportRenderer.render(reportDocuments.submittedDocumentExpiry(idSubmittedDocument));
    }

    public byte[] generateDebtDocumentsReport(Integer numberManagement) {
        return reportRenderer.render(reportDocuments.documentDebt(numberManagement));
    }

    public byte[] generateIndexBookReport(Integer year) throws Exception {
        return generateSimpleTextPdf(
                "Index book",
                "CU24",
                "Period: " + year,
                "Generated: " + LocalDate.now()
        );
    }

    public byte[] generateMonthlyTaxDeclarationReport(Integer year, Integer month) throws Exception {
        return generateSimpleTextPdf(
                "Monthly sworn statement",
                "CU25",
                "Period: " + month + "/" + year,
                "Generated: " + LocalDate.now()
        );
    }

    public byte[] generateTestimonyCopyReport(Integer idTestimony) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException("Testimony not found with ID: " + idTestimony));

        if (!testimony.getVerified()) {
            throw new BusinessValidationException(
                    "The testimony must be verified to issue the printed copy");
        }

        return generateSimpleTextPdf(
                "Testimony copy No. " + testimony.getNumber(),
                "CU08",
                "Verified testimony",
                "Generated: " + LocalDate.now()
        );
    }

    public byte[] generateNotebookCoverReport(Integer idNotebook) {
        Notebook notebook = notebookRepository.findById(idNotebook)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook not found with ID: " + idNotebook));

        int folioFrom = (notebook.getNumber() - 1) * FOLIOS_PER_NOTEBOOK + 1;
        int folioTo = notebook.getNumber() * FOLIOS_PER_NOTEBOOK;

        return generateSimpleTextPdf(
                "Cover of notebook No. " + notebook.getNumber() + "/" + notebook.getYear(),
                "CU80",
                "Registry No. " + notebook.getFkIdNotaryPerson().getNotaryRegistrationNumber()
                        + " - Folios " + folioFrom + " to " + folioTo,
                "Generated: " + LocalDate.now()
        );
    }

    public byte[] generateIncomeTaxDeclarationReport(Integer year, Integer month) throws Exception {
        return generateSimpleTextPdf(
                "Income tax sworn statement",
                "CU50",
                "Period: " + month + "/" + year,
                "Generated: " + LocalDate.now()
        );
    }

    public byte[] generateRegistrationDraftReport(Integer idRegistrationDraft) {
        RegistrationDraft draft = registrationDraftRepository.findById(idRegistrationDraft)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration draft not found with ID: " + idRegistrationDraft));

        return generateSimpleTextPdf(
                "Registration draft N° " + draft.getNumber(),
                "CU82",
                "Deed N° " + draft.getFkIdDeed().getNumber() + " - Status: " + draft.getStatus(),
                "Generated: " + LocalDate.now()
        );
    }

    @Transactional(readOnly = true)
    public byte[] generatePaymentReceiptReport(Integer idPayment) {
        Payment payment = paymentRepository.findById(idPayment)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + idPayment));

        Budget budget = payment.getBudget();
        Person client = budget != null ? budget.getFkIdPerson() : null;
        String nameClient = client != null
                ? (client.getFirstName() + " " + client.getLastName()).trim()
                : "Unidentified client";

        String concepts = budget != null
                ? itemRepository.findByFkIdBudgetIdBudget(budget.getIdBudget()).stream()
                        .map(Item::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("No concepts listed")
                : "No concepts listed";

        return generateReceiptPdf(nameClient, payment.getDate(), concepts, payment.getAmount());
    }

    private byte[] generateReceiptPdf(String client, java.util.Date date, String concepts,
            java.math.BigDecimal amount) {
        try {
            String amountText = amount == null
                    ? "0.00"
                    : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n");
            stream.append("/F1 20 Tf\n");
            stream.append("50 760 Td\n");
            stream.append("(Payment receipt) Tj\n");
            stream.append("/F1 12 Tf\n");
            stream.append("0 -30 Td\n");
            stream.append("(Client: ").append(escapePdfText(client)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Payment date: ").append(escapePdfText(String.valueOf(date))).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Concepts: ").append(escapePdfText(concepts)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(Amount paid: ").append(escapePdfText(amountText)).append(") Tj\n");
            stream.append("0 -40 Td\n");
            stream.append("(Receipt generated by the backend API - CU15.) Tj\n");
            stream.append("ET\n");

            return buildPdf(stream.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate the payment receipt: " + e.getMessage(), e);
        }
    }

    private byte[] generateSimpleTextPdf(String title, String cuId, String periodLine, String dateLine) {
        try {
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n");
            stream.append("/F1 20 Tf\n");
            stream.append("50 760 Td\n");
            stream.append("(").append(escapePdfText(title)).append(") Tj\n");
            stream.append("/F1 12 Tf\n");
            stream.append("0 -30 Td\n");
            stream.append("(Use case: ").append(escapePdfText(cuId)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(").append(escapePdfText(periodLine)).append(") Tj\n");
            stream.append("0 -20 Td\n");
            stream.append("(").append(escapePdfText(dateLine)).append(") Tj\n");
            stream.append("0 -40 Td\n");
            stream.append("(Operational report generated by the backend API.) Tj\n");
            stream.append("ET\n");

            return buildPdf(stream.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate the text PDF: " + e.getMessage(), e);
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
