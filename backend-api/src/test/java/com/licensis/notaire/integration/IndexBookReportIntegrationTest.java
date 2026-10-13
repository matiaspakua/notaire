package com.licensis.notaire.integration;

import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequirementCoverage({"CU24"})
@DisplayName("CU24 index-book PDF on the current schema (#177)")
class IndexBookReportIntegrationTest extends ServiceIntegrationTest {

    private static final int YEAR = 2091;

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

    private MockMvc mockMvc;
    private Integer idNotary;
    private Integer idFolioType;

    @BeforeEach
    void seedIndexPeriod() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Person notary = person("Notary", "Index", "20910001", false, 4411);
        idNotary = notary.getPersonId();
        Person alvarez = person("Lucía", "Álvarez", "20910002", true, null);
        Person pena = person("Ana", "Peña", "20910003", true, null);

        FolioType protocol = new FolioType();
        protocol.setName("Principal 2091");
        protocol.setEnabled(true);
        protocol = folioTypeRepository.save(protocol);
        idFolioType = protocol.getIdFolioType();

        ProcedureType compraventa = procedureType("Compraventa 2091");
        ProcedureType donacion = procedureType("Donación 2091");

        Deed first = deed(881, LocalDate.of(YEAR, 3, 15));
        Deed second = deed(122, LocalDate.of(YEAR, 6, 2));

        folio(7, notary, protocol, first);
        folio(40, notary, protocol, second);
        folio(12, notary, protocol, second);

        procedure(compraventa, first, budgetFor(alvarez, 20910, "Álvarez 2091"));
        procedure(donacion, second, budgetFor(pena, 20911, "Peña 2091"));
    }

    @Test
    @DisplayName("shouldRenderSortedIndexRowsFromRecordedDeeds")
    void shouldRenderSortedIndexRowsFromRecordedDeeds() throws Exception {
        String text = pdfText(YEAR, idNotary, idFolioType);

        assertThat(text).contains("Index book", "CU24", "Álvarez", "Peña",
                "Compraventa 2091", "Donación 2091", "881", "122", "7", "12", "15/03", "02/06");
        assertThat(text.indexOf("Álvarez")).isLessThan(text.indexOf("Peña"));
        assertThat(text).doesNotContain("No deeds were recorded for the requested period.");
    }

    @Test
    @DisplayName("shouldReturnEmptyStatePdfWhenThePeriodHasNoRecordedDeeds")
    void shouldReturnEmptyStatePdfWhenThePeriodHasNoRecordedDeeds() throws Exception {
        String text = pdfText(1800, null, null);

        assertThat(text).contains("Index book", "No deeds were recorded for the requested period.");
    }

    private String pdfText(int year, Integer idNotary, Integer idFolioType) throws Exception {
        var request = get("/api/v1/reports/index-book").param("year", String.valueOf(year));
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
            return new PDFTextStripper().getText(document);
        }
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
        deed.setBody("Index book fixture");
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

    private void procedure(ProcedureType type, Deed deed, Budget budget) {
        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(type);
        procedure.setFkIdDeed(deed);
        procedure.setFkIdBudget(budget);
        procedureRepository.save(procedure);
    }
}
