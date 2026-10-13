package com.licensis.notaire.application.usecase.report;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Table;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU24"})
@DisplayName("IndexBookReportBuilder CU24 rows (#177)")
@ExtendWith(MockitoExtension.class)
class IndexBookReportBuilderTest {

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Test
    @DisplayName("shouldSortRowsByNormalizedLastThenFirstName")
    void shouldSortRowsByNormalizedLastThenFirstName() {
        Person alvarez = person(1, "Lucía", "Álvarez");
        Person pena = person(2, "Ana", "Peña");
        Deed firstDeed = deed(10, 88, LocalDate.of(2026, 3, 15));
        Deed secondDeed = deed(11, 12, LocalDate.of(2026, 6, 2));
        FolioType protocol = folioType(5, "Principal");
        Person notary = notary(20, 4411);
        Folio folioPena = folio(100, 40, 2026, notary, protocol, secondDeed);
        Folio folioAlvarez = folio(101, 7, 2026, notary, protocol, firstDeed);

        when(folioRepository.findByYear(2026)).thenReturn(List.of(folioPena, folioAlvarez));
        when(procedureRepository.findByFkIdDeedIdDeed(10))
                .thenReturn(List.of(procedure(30, "Compraventa", List.of(alvarez), null)));
        when(procedureRepository.findByFkIdDeedIdDeed(11))
                .thenReturn(List.of(procedure(31, "Donación", List.of(pena), null)));

        ReportDocument document = new IndexBookReportBuilder(folioRepository, procedureRepository)
                .build(2026, null, null);

        Table table = (Table) document.sections().get(1);
        assertThat(table.rows()).hasSize(2);
        assertThat(table.rows().get(0).get(0)).contains("Álvarez");
        assertThat(table.rows().get(0).get(1)).isEqualTo("Compraventa");
        assertThat(table.rows().get(0).get(2)).isEqualTo("88");
        assertThat(table.rows().get(0).get(3)).isEqualTo("7");
        assertThat(table.rows().get(0).get(4)).isEqualTo("15/03");
        assertThat(table.rows().get(1).get(0)).contains("Peña");
        assertThat(table.rows().get(1).get(4)).isEqualTo("02/06");
        assertThat(document.identity().emptyStateMessage()).isNull();
    }

    @Test
    @DisplayName("shouldUseMinimumFolioNumberAsFirstFolio")
    void shouldUseMinimumFolioNumberAsFirstFolio() {
        Person client = person(3, "Marta", "Zamora");
        Deed deed = deed(12, 5, LocalDate.of(2026, 1, 8));
        FolioType protocol = folioType(5, "Principal");
        Person notary = notary(20, 4411);
        when(folioRepository.findByYear(2026)).thenReturn(List.of(
                folio(200, 40, 2026, notary, protocol, deed),
                folio(201, 12, 2026, notary, protocol, deed)));
        when(procedureRepository.findByFkIdDeedIdDeed(12))
                .thenReturn(List.of(procedure(32, "Hipoteca", List.of(client), null)));

        Table table = (Table) new IndexBookReportBuilder(folioRepository, procedureRepository)
                .build(2026, null, null)
                .sections()
                .get(1);

        assertThat(table.rows()).hasSize(1);
        assertThat(table.rows().getFirst().get(3)).isEqualTo("12");
    }

    @Test
    @DisplayName("shouldFallBackToBudgetClientWhenPersonListIsEmpty")
    void shouldFallBackToBudgetClientWhenPersonListIsEmpty() {
        Person client = person(4, "Sofía", "Benítez");
        Budget budget = new Budget();
        budget.setFkIdPerson(client);
        Deed deed = deed(13, 3, LocalDate.of(2026, 4, 1));
        FolioType protocol = folioType(5, "Principal");
        Person notary = notary(20, 4411);
        when(folioRepository.findByYear(2026)).thenReturn(List.of(
                folio(202, 9, 2026, notary, protocol, deed)));
        when(procedureRepository.findByFkIdDeedIdDeed(13))
                .thenReturn(List.of(procedure(33, "Sucesión", List.of(), budget)));

        Table table = (Table) new IndexBookReportBuilder(folioRepository, procedureRepository)
                .build(2026, null, null)
                .sections()
                .get(1);

        assertThat(table.rows().getFirst().get(0)).contains("Benítez");
        assertThat(table.rows().getFirst().get(1)).isEqualTo("Sucesión");
    }

    @Test
    @DisplayName("shouldExcludeFoliosOutsideOptionalNotaryAndProtocolFilters")
    void shouldExcludeFoliosOutsideOptionalNotaryAndProtocolFilters() {
        Person client = person(5, "Eva", "Costa");
        Deed keep = deed(14, 1, LocalDate.of(2026, 5, 5));
        Deed drop = deed(15, 2, LocalDate.of(2026, 5, 6));
        FolioType principal = folioType(5, "Principal");
        FolioType auxiliary = folioType(6, "Auxiliar");
        Person notaryA = notary(20, 4411);
        Person notaryB = notary(21, 5522);
        when(folioRepository.findByYear(2026)).thenReturn(List.of(
                folio(203, 1, 2026, notaryA, principal, keep),
                folio(204, 2, 2026, notaryB, principal, drop),
                folio(205, 3, 2026, notaryA, auxiliary, drop)));
        when(procedureRepository.findByFkIdDeedIdDeed(14))
                .thenReturn(List.of(procedure(34, "Poder", List.of(client), null)));

        Table table = (Table) new IndexBookReportBuilder(folioRepository, procedureRepository)
                .build(2026, 20, 5)
                .sections()
                .get(1);

        assertThat(table.rows()).hasSize(1);
        assertThat(table.rows().getFirst().get(2)).isEqualTo("1");
    }

    @Test
    @DisplayName("shouldEmitEmptyStateWhenNoRecordedDeedMatches")
    void shouldEmitEmptyStateWhenNoRecordedDeedMatches() {
        when(folioRepository.findByYear(1999)).thenReturn(List.of());

        ReportDocument document = new IndexBookReportBuilder(folioRepository, procedureRepository)
                .build(1999, null, null);

        assertThat(document.identity().emptyStateMessage())
                .isEqualTo(IndexBookReportBuilder.EMPTY_PERIOD);
        Table table = (Table) document.sections().get(1);
        assertThat(table.rows()).isEmpty();
        assertThat(table.emptyMessage()).isEqualTo(IndexBookReportBuilder.EMPTY_PERIOD);
    }

    @Test
    @DisplayName("shouldIgnoreFoliosWithoutALinkedDeed")
    void shouldIgnoreFoliosWithoutALinkedDeed() {
        when(folioRepository.findByYear(2026)).thenReturn(List.of(
                folio(206, 1, 2026, notary(20, 1), folioType(5, "Principal"), null)));

        ReportDocument document = new IndexBookReportBuilder(folioRepository, procedureRepository)
                .build(2026, null, null);

        assertThat(((Table) document.sections().get(1)).rows()).isEmpty();
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

    private static FolioType folioType(int id, String name) {
        FolioType type = new FolioType();
        type.setIdFolioType(id);
        type.setName(name);
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
        folio.setStatus("Utilizado");
        folio.setFkIdNotaryPerson(notary);
        folio.setFkIdFolioType(type);
        folio.setFkIdDeed(deed);
        return folio;
    }

    private static Procedure procedure(int id, String typeName, List<Person> people, Budget budget) {
        ProcedureType type = new ProcedureType();
        type.setName(typeName);
        Procedure procedure = new Procedure();
        procedure.setIdProcedure(id);
        procedure.setFkIdProcedureType(type);
        procedure.setPersonList(people);
        procedure.setFkIdBudget(budget);
        return procedure;
    }
}
