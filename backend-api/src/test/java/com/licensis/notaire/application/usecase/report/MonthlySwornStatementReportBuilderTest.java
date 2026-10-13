package com.licensis.notaire.application.usecase.report;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Table;
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
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.RegistrationDraftRepository;
import com.licensis.notaire.repository.SubstitutionRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU25"})
@DisplayName("MonthlySwornStatementReportBuilder CU25 rows (#178)")
@ExtendWith(MockitoExtension.class)
class MonthlySwornStatementReportBuilderTest {

    @Mock
    private FolioRepository folioRepository;
    @Mock
    private ProcedureRepository procedureRepository;
    @Mock
    private SubstitutionRepository substitutionRepository;
    @Mock
    private RegistrationDraftRepository registrationDraftRepository;

    @Test
    @DisplayName("shouldIncludeMarchDeedAndExcludeJuneDeed")
    void shouldIncludeMarchDeedAndExcludeJuneDeed() {
        Person client = person(3, "Lucia", "Zamora");
        Deed march = deed(10, 881, LocalDate.of(2026, 3, 15));
        Deed june = deed(11, 122, LocalDate.of(2026, 6, 2));
        FolioType protocol = folioType(5, "Principal", false);
        Person notary = notary(20, 4411);
        when(folioRepository.findByYear(2026)).thenReturn(List.of(
                folio(100, 7, 2026, notary, protocol, march),
                folio(101, 40, 2026, notary, protocol, june)));
        when(procedureRepository.findByFkIdDeedIdDeed(10))
                .thenReturn(List.of(procedure(30, "Compraventa", List.of(client), null, null)));
        when(registrationDraftRepository.findByFkIdDeedIdDeed(10)).thenReturn(Optional.empty());
        when(substitutionRepository.findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                eq(20), any(), any())).thenReturn(List.of());

        ReportDocument document = builder().build(2026, 3, 20, null);
        Table table = (Table) document.sections().get(1);

        assertThat(table.rows()).hasSize(1);
        assertThat(table.rows().getFirst().get(0)).isEqualTo("881");
        assertThat(table.rows().getFirst().get(1)).isEqualTo("7");
        assertThat(table.rows().getFirst().get(2)).isEqualTo("15");
        assertThat(table.rows().getFirst().get(3)).contains("Compraventa").contains("Zamora");
        assertThat(table.rows().getFirst().get(4)).isEqualTo(ReportLocaleFormats.NOT_RECORDED);
        assertThat(table.rows().getFirst().get(8)).isEqualTo(ReportLocaleFormats.NOT_RECORDED);
        assertThat(document.title()).isEqualTo("Monthly sworn statement");
        assertThat(document.identity().emptyStateMessage()).isNull();
    }

    @Test
    @DisplayName("shouldShowSubstituteWhenASubstitutionCoversTheMonth")
    void shouldShowSubstituteWhenASubstitutionCoversTheMonth() {
        Person client = person(3, "Lucia", "Zamora");
        Person titular = notary(20, 4411);
        Person substitute = person(21, "Marta", "Suplente");
        Deed march = deed(10, 881, LocalDate.of(2026, 3, 15));
        FolioType protocol = folioType(5, "Principal", false);
        Substitution substitution = new Substitution();
        substitution.setFkIdSubstituted(titular);
        substitution.setFkIdSubstitute(substitute);
        when(folioRepository.findByYear(2026)).thenReturn(List.of(
                folio(100, 7, 2026, titular, protocol, march)));
        when(procedureRepository.findByFkIdDeedIdDeed(10))
                .thenReturn(List.of(procedure(30, "Compraventa", List.of(client), null, null)));
        when(registrationDraftRepository.findByFkIdDeedIdDeed(10)).thenReturn(Optional.empty());
        when(substitutionRepository.findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                eq(20), any(), any())).thenReturn(List.of(substitution));

        Fields header = (Fields) builder().build(2026, 3, 20, null).sections().getFirst();
        assertThat(header.fields().stream().map(field -> field.label() + "=" + field.value()).toList())
                .anyMatch(line -> line.contains("Substitute") && line.contains("Suplente"));
        assertThat(header.fields().stream().map(field -> field.label() + "=" + field.value()).toList())
                .anyMatch(line -> line.contains("Titular") && line.contains("Registry"));
    }

    @Test
    @DisplayName("shouldPrintPropertyAndDraftPriceOnPrincipalProtocol")
    void shouldPrintPropertyAndDraftPriceOnPrincipalProtocol() {
        Person client = person(3, "Lucia", "Zamora");
        Deed march = deed(10, 881, LocalDate.of(2026, 3, 15));
        FolioType protocol = folioType(5, "Principal", false);
        Property property = new Property();
        property.setCadastralDesignation("Circ 1 Sec 2");
        property.setFiscalAppraisal(new BigDecimal("150000.50"));
        RegistrationDraft draft = new RegistrationDraft();
        draft.setOperationPrice(new BigDecimal("200000"));
        when(folioRepository.findByYear(2026)).thenReturn(List.of(
                folio(100, 7, 2026, notary(20, 4411), protocol, march)));
        when(procedureRepository.findByFkIdDeedIdDeed(10))
                .thenReturn(List.of(procedure(30, "Compraventa", List.of(client), null, property)));
        when(registrationDraftRepository.findByFkIdDeedIdDeed(10)).thenReturn(Optional.of(draft));
        when(substitutionRepository.findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                eq(20), any(), any())).thenReturn(List.of());

        Table table = (Table) builder().build(2026, 3, 20, null).sections().get(1);
        assertThat(table.rows().getFirst().get(5)).isEqualTo("Circ 1 Sec 2");
        assertThat(table.rows().getFirst().get(6)).isEqualTo(ReportLocaleFormats.money(new BigDecimal("150000.50")));
        assertThat(table.rows().getFirst().get(7)).isEqualTo(ReportLocaleFormats.money(new BigDecimal("200000")));
        assertThat(table.totals().stream().map(field -> field.value()).toList())
                .contains(ReportLocaleFormats.money(new BigDecimal("150000.50")),
                        ReportLocaleFormats.money(new BigDecimal("200000")));
    }

    @Test
    @DisplayName("shouldEmitEmptyStateWhenTheMonthHasNoRecordedDeed")
    void shouldEmitEmptyStateWhenTheMonthHasNoRecordedDeed() {
        when(folioRepository.findByYear(1999)).thenReturn(List.of());

        ReportDocument document = builder().build(1999, 4, null, null);
        assertThat(document.identity().emptyStateMessage())
                .isEqualTo(MonthlySwornStatementReportBuilder.EMPTY_PERIOD);
        Table table = (Table) document.sections().get(1);
        assertThat(table.rows()).isEmpty();
    }

    private MonthlySwornStatementReportBuilder builder() {
        return new MonthlySwornStatementReportBuilder(
                folioRepository, procedureRepository, substitutionRepository, registrationDraftRepository);
    }

    private static Person person(int id, String first, String last) {
        Person person = new Person();
        person.setPersonId(id);
        person.setFirstName(first);
        person.setLastName(last);
        return person;
    }

    private static Person notary(int id, int registration) {
        Person person = person(id, "Notary", "Registry");
        person.setNotaryRegistrationNumber(registration);
        return person;
    }

    private static FolioType folioType(int id, String name, boolean auxiliary) {
        FolioType type = new FolioType();
        type.setIdFolioType(id);
        type.setName(name);
        type.setIsAuxiliary(auxiliary);
        return type;
    }

    private static Deed deed(int id, int number, LocalDate recording) {
        Deed deed = new Deed();
        deed.setIdDeed(id);
        deed.setNumber(number);
        deed.setDateDeedrecording(Date.valueOf(recording));
        return deed;
    }

    private static Folio folio(int id, int number, int year, Person notary, FolioType type, Deed deed) {
        Folio folio = new Folio();
        folio.setIdFolio(id);
        folio.setNumber(number);
        folio.setYear(year);
        folio.setFkIdNotaryPerson(notary);
        folio.setFkIdFolioType(type);
        folio.setFkIdDeed(deed);
        return folio;
    }

    private static Procedure procedure(int id, String typeName, List<Person> people, Budget budget, Property property) {
        ProcedureType type = new ProcedureType();
        type.setName(typeName);
        Procedure procedure = new Procedure();
        procedure.setIdProcedure(id);
        procedure.setFkIdProcedureType(type);
        procedure.setPersonList(people);
        procedure.setFkIdBudget(budget);
        procedure.setFkIdProperty(property);
        return procedure;
    }
}
