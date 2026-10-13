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
import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Identity;
import com.licensis.notaire.application.port.out.report.ReportRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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

    public byte[] generateIndexBookReport(Integer year) {
        return renderPlaceholder("Index book", "CU24", "index-book-" + year, "Period: " + year);
    }

    public byte[] generateMonthlyTaxDeclarationReport(Integer year, Integer month) {
        return renderPlaceholder("Monthly sworn statement", "CU25",
                "monthly-sworn-" + year + "-" + month, "Period: " + month + "/" + year);
    }

    public byte[] generateTestimonyCopyReport(Integer idTestimony) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException("Testimony not found with ID: " + idTestimony));

        if (!testimony.getVerified()) {
            throw new BusinessValidationException(
                    "The testimony must be verified to issue the printed copy");
        }

        return renderPlaceholder(
                "Testimony copy No. " + testimony.getNumber(),
                "CU08",
                "testimony-" + testimony.getNumber(),
                "Verified testimony");
    }

    public byte[] generateNotebookCoverReport(Integer idNotebook) {
        Notebook notebook = notebookRepository.findById(idNotebook)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook not found with ID: " + idNotebook));

        int folioFrom = (notebook.getNumber() - 1) * FOLIOS_PER_NOTEBOOK + 1;
        int folioTo = notebook.getNumber() * FOLIOS_PER_NOTEBOOK;

        return renderPlaceholder(
                "Cover of notebook No. " + notebook.getNumber() + "/" + notebook.getYear(),
                "CU80",
                "notebook-" + notebook.getNumber(),
                "Registry No. " + notebook.getFkIdNotaryPerson().getNotaryRegistrationNumber()
                        + " - Folios " + folioFrom + " to " + folioTo);
    }

    public byte[] generateIncomeTaxDeclarationReport(Integer year, Integer month) {
        return renderPlaceholder("Income tax sworn statement", "CU50",
                "tax-sworn-" + year + "-" + month, "Period: " + month + "/" + year);
    }

    public byte[] generateRegistrationDraftReport(Integer idRegistrationDraft) {
        RegistrationDraft draft = registrationDraftRepository.findById(idRegistrationDraft)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration draft not found with ID: " + idRegistrationDraft));

        return renderPlaceholder(
                "Registration draft N° " + draft.getNumber(),
                "CU82",
                "registration-draft-" + draft.getNumber(),
                "Deed N° " + draft.getFkIdDeed().getNumber() + " - Status: " + draft.getStatus());
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

        LocalDate paymentDay = payment.getDate() == null
                ? null
                : payment.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return reportRenderer.render(new ReportDocument(
                "Payment receipt",
                "CU15",
                List.of(new Fields("Receipt", List.of(
                        new Field("Client", nameClient),
                        new Field("Payment date", ReportLocaleFormats.day(paymentDay)),
                        new Field("Concepts", concepts),
                        new Field("Amount paid", ReportLocaleFormats.money(payment.getAmount()))))),
                Identity.of("CU15", "payment-" + payment.getIdPayment(), null)));
    }

    private byte[] renderPlaceholder(String title, String cuId, String identifier, String periodLine) {
        return reportRenderer.render(new ReportDocument(
                title,
                cuId,
                List.of(new Fields(null, List.of(
                        new Field("Use case", cuId),
                        new Field("Detail", periodLine),
                        new Field("Notes", "Operational report generated by the backend API.")))),
                Identity.of(cuId, identifier, periodLine)));
    }
}
