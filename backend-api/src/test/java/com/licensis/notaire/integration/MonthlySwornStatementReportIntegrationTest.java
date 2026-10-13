package com.licensis.notaire.integration;

import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.PropertyRepository;
import com.licensis.notaire.repository.RegistrationDraftRepository;
import com.licensis.notaire.repository.SubstitutionRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequirementCoverage({"CU25"})
@DisplayName("CU25 monthly sworn-statement PDF on the current schema (#178)")
class MonthlySwornStatementReportIntegrationTest extends ServiceIntegrationTest {

    private static final int YEAR = 2092;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private FolioTypeRepository folioTypeRepository;
    @Autowired
    private FolioRepository folioRepository;
    @Autowired
    private DeedRepository deedRepository;
    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;
    @Autowired
    private ProcedureRepository procedureRepository;
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private RegistrationDraftRepository registrationDraftRepository;
    @Autowired
    private SubstitutionRepository substitutionRepository;

    private MockMvc mockMvc;
    private Integer idNotary;
    private Integer idFolioType;

    @BeforeEach
    void seedMonthlyPeriod() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Person notary = person("Notary", "Registry", "20920001", false, 5522);
        idNotary = notary.getPersonId();
        Person client = person("Lucía", "Álvarez", "20920002", true, null);
        Person substitute = person("Marta", "Suplente2092", "20920003", false, null);

        FolioType protocol = new FolioType();
        protocol.setName("Principal 2092");
        protocol.setEnabled(true);
        protocol.setIsAuxiliary(false);
        protocol = folioTypeRepository.save(protocol);
        idFolioType = protocol.getIdFolioType();

        Property property = new Property();
        property.setCadastralDesignation("Circ 1 Sec 2 2092");
        property.setAddress("Calle Falsa 2092");
        property.setFiscalAppraisal(new BigDecimal("150000.50"));
        property = propertyRepository.save(property);

        ProcedureType compraventa = procedureType("Compraventa 2092");

        Deed march = deed(881, LocalDate.of(YEAR, 3, 15));
        Deed june = deed(122, LocalDate.of(YEAR, 6, 2));

        folio(7, notary, protocol, march);
        folio(40, notary, protocol, june);

        procedure(compraventa, march, budgetFor(client, 20920, "Álvarez 2092"), property);
        procedure(compraventa, june, budgetFor(client, 20921, "Álvarez June 2092"), null);

        RegistrationDraft draft = new RegistrationDraft();
        draft.setNumber(20920);
        draft.setStatus("Generada");
        draft.setDateGeneration(Date.valueOf(LocalDate.of(YEAR, 3, 16)));
        draft.setOperationPrice(new BigDecimal("200000"));
        draft.setFkIdDeed(march);
        registrationDraftRepository.save(draft);

        Substitution substitution = new Substitution();
        substitution.setFkIdSubstituted(notary);
        substitution.setFkIdSubstitute(substitute);
        substitution.setDateStart(Date.valueOf(LocalDate.of(YEAR, 1, 1)));
        substitution.setDateEnd(Date.valueOf(LocalDate.of(YEAR, 12, 31)));
        substitutionRepository.save(substitution);
    }

    @Test
    @DisplayName("shouldRenderMarchDeedAndExcludeJuneDeed")
    void shouldRenderMarchDeedAndExcludeJuneDeed() throws Exception {
        PdfResult result = pdf(YEAR, 3, idNotary, idFolioType);

        assertThat(result.text).contains("Monthly sworn statement", "CU25", "Álvarez",
                "Compraventa 2092", "881", "7", "15", "Circ 1 Sec 2 2092", "Suplente2092", "N/A");
        assertThat(result.text).doesNotContain("122");
        assertThat(result.text).doesNotContain("No deeds were recorded for the requested period.");
        assertThat(result.pageSize.getWidth()).isGreaterThan(result.pageSize.getHeight());
        assertThat(result.pageSize.getWidth()).isEqualTo(PDRectangle.A4.getHeight());
        assertThat(result.pageSize.getHeight()).isEqualTo(PDRectangle.A4.getWidth());
    }

    @Test
    @DisplayName("shouldReturnEmptyStatePdfWhenTheMonthHasNoRecordedDeed")
    void shouldReturnEmptyStatePdfWhenTheMonthHasNoRecordedDeed() throws Exception {
        PdfResult result = pdf(1800, 4, null, null);

        assertThat(result.text).contains("Monthly sworn statement",
                "No deeds were recorded for the requested period.");
    }

    private PdfResult pdf(int year, int month, Integer idNotary, Integer idFolioType) throws Exception {
        var request = get("/api/v1/reports/monthly-sworn-statement")
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month));
        if (idNotary != null) {
            request = request.param("idNotary", String.valueOf(idNotary));
        }
        if (idFolioType != null) {
            request = request.param("idFolioType", String.valueOf(idFolioType));
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(new String(body, 0, 5)).startsWith("%PDF-");
        try (PDDocument document = Loader.loadPDF(body)) {
            return new PdfResult(
                    new PDFTextStripper().getText(document),
                    document.getPage(0).getMediaBox());
        }
    }

    private record PdfResult(String text, PDRectangle pageSize) {
    }

    private Person person(String first, String last, String identification, boolean client, Integer registration) {
        Person person = new Person();
        person.setFirstName(first);
        person.setLastName(last);
        person.setIdentificationNumber(identification);
        person.setIsClient(client);
        person.setNotaryRegistrationNumber(registration);
        person.setFkIdIdentificationType(identificationTypeRepository.findById(1).orElseThrow());
        return personRepository.save(person);
    }

    private ProcedureType procedureType(String name) {
        ProcedureType type = new ProcedureType();
        type.setName(name);
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        return procedureTypeRepository.save(type);
    }

    private Deed deed(int number, LocalDate recording) {
        Deed deed = new Deed();
        deed.setNumber(number);
        deed.setDateDeedrecording(Date.valueOf(recording));
        deed.setStatus("Sin Firmar");
        deed.setBody("Monthly sworn fixture");
        return deedRepository.save(deed);
    }

    private void folio(int number, Person notary, FolioType type, Deed deed) {
        Folio folio = new Folio();
        folio.setNumber(number);
        folio.setYear(YEAR);
        folio.setStatus("Utilizado");
        folio.setFkIdNotaryPerson(notary);
        folio.setFkIdFolioType(type);
        folio.setFkIdDeed(deed);
        folioRepository.save(folio);
    }

    private Budget budgetFor(Person client, int number, String heading) {
        Budget budget = new Budget();
        budget.setNumber(number);
        budget.setDate(Date.valueOf(LocalDate.of(YEAR, 5, 1)));
        budget.setHeading(heading);
        budget.setStatus("Pending");
        budget.setFkIdPerson(client);
        return budgetRepository.save(budget);
    }

    private void procedure(ProcedureType type, Deed deed, Budget budget, Property property) {
        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(type);
        procedure.setFkIdDeed(deed);
        procedure.setFkIdBudget(budget);
        procedure.setFkIdProperty(property);
        procedureRepository.save(procedure);
    }
}
