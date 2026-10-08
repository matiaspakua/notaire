package com.licensis.notaire.integration;

import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.PropertyRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import jakarta.persistence.EntityManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The six reports that used JasperReports 3.5.3 templates written against the legacy
 * MySQL schema (issue #567) are rendered in-house from the current domain model. Each
 * one must return a real PDF with the data of the requested aggregate, and a 404 when
 * that aggregate does not exist.
 */
@RequirementCoverage({"CU01", "CU03", "CU09", "CU13", "CU42"})
@DisplayName("In-house report PDFs on the current schema (issue #567)")
class InHouseReportPdfIntegrationTest extends ServiceIntegrationTest {

    private static final int MANAGEMENT_NUMBER = 56701;
    private static final String PROCEDURE_TYPE = "Compraventa 567";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;
    @Autowired
    private ProcedureTemplateRepository procedureTemplateRepository;
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private ManagementStatusRepository managementStatusRepository;
    @Autowired
    private DeedManagementRepository managementRepository;
    @Autowired
    private ProcedureRepository procedureRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private SubmittedDocumentRepository submittedDocumentRepository;

    private MockMvc mockMvc;
    private Integer budgetId;
    private Integer managementId;
    private Integer submittedDocumentId;
    private Date dueDate;

    @BeforeEach
    void seedCurrentSchemaData() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Person client = new Person();
        client.setFirstName("Lucía");
        client.setLastName("Peña");
        client.setIdentificationNumber("56701");
        client.setIsClient(true);
        client.setFkIdIdentificationType(identificationTypeRepository.findById(1).orElseThrow());
        client = personRepository.save(client);

        Budget budget = new Budget();
        budget.setNumber(MANAGEMENT_NUMBER);
        budget.setDate(date(LocalDate.of(2026, 9, 15)));
        budget.setEncabezado("Compraventa Peña");
        budget.setStatus("Pending");
        budget.setPropertyAmount(new BigDecimal("150000.00"));
        budget.setFkIdPerson(client);
        budget = budgetRepository.save(budget);
        budgetId = budget.getIdBudget();
        itemRepository.save(item(budget, "Honorarios escribanía", "10000.00"));
        itemRepository.save(item(budget, "Sellado", "2500.00"));

        ProcedureType procedureType = new ProcedureType();
        procedureType.setName(PROCEDURE_TYPE);
        procedureType.setEnabled(true);
        procedureType.setIsArchived(true);
        procedureType.setIsRegistered(true);
        procedureType.setAssociatesProperties(true);
        procedureType = procedureTypeRepository.save(procedureType);

        DocumentType ownershipReport = documentType("Informe de dominio 567", true, 30, "Registro de la Propiedad");
        DocumentType cadastralCertificate = documentType("Certificado catastral 567", false, null, "Catastro");
        procedureTemplateRepository.save(new ProcedureTemplate(procedureType.getIdProcedureType(),
                ownershipReport.getIdDocumentType()));
        procedureTemplateRepository.save(new ProcedureTemplate(procedureType.getIdProcedureType(),
                cadastralCertificate.getIdDocumentType()));

        Property property = new Property();
        property.setCadastralDesignation("01-02-003-004");
        property.setFiscalAppraisal(new BigDecimal("98765.43"));
        property.setAddress("Calle Falsa 123");
        property = propertyRepository.save(property);

        ManagementStatus status = managementStatusRepository.findById(1).orElseThrow();
        DeedManagement management = new DeedManagement();
        management.setNumber(MANAGEMENT_NUMBER);
        management.setDateStart(date(LocalDate.of(2026, 9, 16)));
        management.setEncabezado("Gestión Peña");
        management.setFkIdNotaryPerson(client);
        management.setFkIdManagementStatus(status);
        management = managementRepository.save(management);
        managementId = management.getIdManagement();

        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(procedureType);
        procedure.setFkIdBudget(budget);
        procedure.setFkIdManagement(management);
        procedure.setFkIdProperty(property);
        procedure = procedureRepository.save(procedure);

        historyRepository.save(history(management, status, LocalDate.of(2026, 9, 16), "Inicio de gestión"));
        historyRepository.save(history(management, status, LocalDate.of(2026, 9, 20), "Documentación presentada"));

        dueDate = date(LocalDate.now().plusDays(10));
        SubmittedDocument document = new SubmittedDocument();
        document.setName("Informe de dominio");
        document.setDocumentType(ownershipReport);
        document.setFkIdProcedure(procedure);
        document.setPrepared(true);
        document.setExpires(true);
        document.setDeliveredBy("Registro de la Propiedad");
        document.setCardNumber(77);
        document.setFlagged(false);
        document.setDateEntry(date(LocalDate.of(2026, 9, 20)));
        document.setDateDue(dueDate);
        document.setAmountToPay(new BigDecimal("3000.00"));
        submittedDocumentId = submittedDocumentRepository.save(document).getIdSubmittedDocument();

        // The reports read the aggregates back through their associations, as in production.
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("shouldRenderTheBudgetReportFromItsItemsAndCharges")
    void shouldRenderTheBudgetReportFromItsItemsAndCharges() throws Exception {
        String text = pdfText(get("/api/v1/reportes/presupuesto/{id}", budgetId));

        assertThat(text).contains("Presupuesto N° " + MANAGEMENT_NUMBER, "Lucía Peña", "15/09/2026",
                PROCEDURE_TYPE, "Honorarios escribanía", "$ 10.000,00", "Sellado", "$ 2.500,00");
        // CU47 total: item lines plus the cost of the documents submitted for the budget.
        assertThat(text).contains("Total", "$ 15.500,00", "Saldo pendiente");
        assertThat(text).doesNotContain("01-02-003-004");
    }

    @Test
    @DisplayName("shouldRenderTheBudgetReportWithTheLinkedProperties")
    void shouldRenderTheBudgetReportWithTheLinkedProperties() throws Exception {
        String text = pdfText(get("/api/v1/reportes/presupuesto-inmuebles/{id}", budgetId));

        assertThat(text).contains("Presupuesto N° " + MANAGEMENT_NUMBER, "Honorarios escribanía",
                "Inmuebles", "01-02-003-004", "$ 98.765,43", "Calle Falsa 123", "$ 15.500,00");
    }

    @Test
    @DisplayName("shouldRenderTheRequiredDocumentsOfAProcedureType")
    void shouldRenderTheRequiredDocumentsOfAProcedureType() throws Exception {
        String text = pdfText(get("/api/v1/reportes/lista-documentos-tramite")
                .param("nombreTipoTramite", PROCEDURE_TYPE));

        assertThat(text).contains(PROCEDURE_TYPE, "Se archiva", "Sí", "Informe de dominio 567", "30",
                "Registro de la Propiedad", "Certificado catastral 567", "Catastro");
    }

    @Test
    @DisplayName("shouldRenderTheHistoryOfAManagement")
    void shouldRenderTheHistoryOfAManagement() throws Exception {
        String text = pdfText(get("/api/v1/reportes/historial-gestion/{id}", managementId));

        assertThat(text).contains("Gestión N° " + MANAGEMENT_NUMBER, "Gestión Peña", "Lucía Peña",
                PROCEDURE_TYPE, "16/09/2026", "Inicio de gestión", "20/09/2026", "Documentación presentada");
        assertThat(text.indexOf("Inicio de gestión")).isLessThan(text.indexOf("Documentación presentada"));
    }

    @Test
    @DisplayName("shouldRenderTheExpiryDetailOfASubmittedDocument")
    void shouldRenderTheExpiryDetailOfASubmittedDocument() throws Exception {
        String text = pdfText(get("/api/v1/reportes/documentos-por-vencer/{id}", submittedDocumentId));

        assertThat(text).contains("Informe de dominio", "Informe de dominio 567",
                String.valueOf(MANAGEMENT_NUMBER), "Gestión Peña", "Lucía Peña", "77", "$ 3.000,00",
                new SimpleDateFormat("dd/MM/yyyy").format(dueDate), "Vence en 10 días");
    }

    @Test
    @DisplayName("shouldRenderTheDocumentDebtOfAManagementNumber")
    void shouldRenderTheDocumentDebtOfAManagementNumber() throws Exception {
        String text = pdfText(get("/api/v1/reportes/consultar-deuda-documentos")
                .param("numberManagement", String.valueOf(MANAGEMENT_NUMBER)));

        assertThat(text).contains(PROCEDURE_TYPE, "Informe de dominio", "$ 3.000,00", "Pago pendiente",
                "Total adeudado");
    }

    @Test
    @DisplayName("shouldAnswer404WhenTheReportedAggregateDoesNotExist")
    void shouldAnswer404WhenTheReportedAggregateDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/presupuesto/{id}", 999_999)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/{id}", 999_999))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite").param("nombreTipoTramite", "No existe 567"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/reportes/historial-gestion/{id}", 999_999)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/reportes/documentos-por-vencer/{id}", 999_999))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/reportes/consultar-deuda-documentos").param("numberManagement", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("shouldNoLongerShipJasperReports")
    void shouldNoLongerShipJasperReports() {
        assertThatThrownBy(() -> Class.forName("net.sf.jasperreports.engine.JasperReport"))
                .isInstanceOf(ClassNotFoundException.class);
        assertThat(getClass().getClassLoader().getResource("reportes/reportePresupuestoSinInmueble.jasper")).isNull();
    }

    private String pdfText(RequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(new String(body, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (PDDocument document = Loader.loadPDF(body)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static Date date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Item item(Budget budget, String name, String value) {
        Item item = new Item();
        item.setName(name);
        item.setValue(new BigDecimal(value));
        item.setType(TypeItem.NORMAL);
        item.setFkIdBudget(budget);
        return item;
    }

    private DocumentType documentType(String name, boolean expires, Integer dueDays, String deliveredBy) {
        DocumentType documentType = new DocumentType();
        documentType.setName(name);
        documentType.setEnabled(true);
        documentType.setExpires(expires);
        documentType.setDueDays(dueDays);
        documentType.setDeliveredBy(deliveredBy);
        return documentTypeRepository.save(documentType);
    }

    private static History history(DeedManagement management, ManagementStatus status, LocalDate on, String notes) {
        History history = new History();
        history.setDate(date(on));
        history.setNotes(notes);
        history.setFkIdManagement(management);
        history.setFkIdManagementStatus(status);
        return history;
    }
}
