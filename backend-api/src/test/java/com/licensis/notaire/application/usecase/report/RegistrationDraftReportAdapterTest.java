package com.licensis.notaire.application.usecase.report;

import com.licensis.notaire.adapter.out.report.RegistrationDraftReportAdapter;
import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.RegistrationDraftRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU82"})
@DisplayName("RegistrationDraftReportAdapter CU82 rows (#116)")
@ExtendWith(MockitoExtension.class)
class RegistrationDraftReportAdapterTest {

    @Mock
    private RegistrationDraftRepository registrationDraftRepository;
    @Mock
    private ProcedureRepository procedureRepository;

    @Test
    @DisplayName("generated draft prints recorded cadastral designation")
    void shouldPrintRecordedCadastralDesignation() {
        RegistrationDraft draft = draft(14, "Generada", new BigDecimal("150000.50"),
                deed(881), null);
        when(registrationDraftRepository.findById(7)).thenReturn(Optional.of(draft));
        when(procedureRepository.findByFkIdDeedIdDeed(3)).thenReturn(List.of(procedure(
                property("NC-CU82-PRINT", "MAT-9", "Volume 1 Folio 2",
                        "North, South, East, West", new BigDecimal("100000"), "Street 1"),
                List.of(person("Ana", "Gomez")))));

        ReportDocument document = adapter().build(7);
        Fields fields = (Fields) document.sections().getFirst();

        assertThat(document.title()).isEqualTo("Registration draft");
        assertThat(document.subtitle()).isEqualTo("CU82");
        assertThat(value(fields, "Draft")).isEqualTo("14");
        assertThat(value(fields, "Deed")).isEqualTo("881");
        assertThat(value(fields, "Cadastral")).isEqualTo("NC-CU82-PRINT");
        assertThat(value(fields, "Registration")).isEqualTo("MAT-9");
        assertThat(value(fields, "Boundaries")).isEqualTo("North, South, East, West");
        assertThat(value(fields, "Parties")).isEqualTo("Ana Gomez");
        assertThat(value(fields, "Price")).isEqualTo(ReportLocaleFormats.money(new BigDecimal("150000.50")));
        assertThat(fields.fields().stream().map(Field::value).toList())
                .noneMatch(value -> value != null && value.toLowerCase().contains("signature"));
    }

    @Test
    @DisplayName("missing optional fields stay N/A")
    void shouldUseNaWhenOptionalFieldsMissing() {
        RegistrationDraft draft = draft(9, "Generada", null, deed(12), null);
        when(registrationDraftRepository.findById(8)).thenReturn(Optional.of(draft));
        when(procedureRepository.findByFkIdDeedIdDeed(3)).thenReturn(List.of());

        Fields fields = (Fields) adapter().build(8).sections().getFirst();

        assertThat(value(fields, "Draft")).isEqualTo("9");
        assertThat(value(fields, "Price")).isEqualTo(ReportLocaleFormats.NOT_RECORDED);
        assertThat(value(fields, "Parties")).isEqualTo(ReportLocaleFormats.NOT_RECORDED);
        assertThat(value(fields, "Cadastral")).isEqualTo(ReportLocaleFormats.NOT_RECORDED);
    }

    @Test
    @DisplayName("registry notes appear when recorded")
    void shouldPrintRegistryNotes() {
        RegistrationDraft draft = draft(5, "Observado", null, deed(20),
                "Missing cadastral certificate");
        when(registrationDraftRepository.findById(3)).thenReturn(Optional.of(draft));
        when(procedureRepository.findByFkIdDeedIdDeed(3)).thenReturn(List.of());

        Fields fields = (Fields) adapter().build(3).sections().getFirst();

        assertThat(value(fields, "Notes")).isEqualTo("Missing cadastral certificate");
        assertThat(value(fields, "Status")).isEqualTo("Observado");
    }

    @Test
    @DisplayName("unknown draft is not found")
    void shouldRejectUnknown() {
        when(registrationDraftRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter().build(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private RegistrationDraftReportAdapter adapter() {
        return new RegistrationDraftReportAdapter(registrationDraftRepository, procedureRepository);
    }

    private static RegistrationDraft draft(int number, String status, BigDecimal price,
                                           Deed deed, String notes) {
        RegistrationDraft draft = new RegistrationDraft();
        draft.setIdRegistrationDraft(1);
        draft.setNumber(number);
        draft.setStatus(status);
        draft.setOperationPrice(price);
        draft.setDateGeneration(Date.valueOf(LocalDate.of(2026, 6, 16)));
        draft.setFkIdDeed(deed);
        draft.setRegistryNotes(notes);
        return draft;
    }

    private static Deed deed(int number) {
        Deed deed = new Deed();
        deed.setIdDeed(3);
        deed.setNumber(number);
        deed.setDateDeedrecording(Date.valueOf(LocalDate.of(2026, 6, 16)));
        return deed;
    }

    private static Procedure procedure(Property property, List<Person> people) {
        Procedure procedure = new Procedure();
        procedure.setFkIdProperty(property);
        procedure.setPersonList(people);
        return procedure;
    }

    private static Property property(String cadastral, String registration, String volume,
                                     String boundaries, BigDecimal fiscal, String address) {
        Property property = new Property();
        property.setCadastralDesignation(cadastral);
        property.setRegistrationNumber(registration);
        property.setVolumeFolioLandRecord(volume);
        property.setBoundaries(boundaries);
        property.setFiscalAppraisal(fiscal);
        property.setAddress(address);
        return property;
    }

    private static Person person(String first, String last) {
        Person person = new Person();
        person.setFirstName(first);
        person.setLastName(last);
        return person;
    }

    private static String value(Fields fields, String label) {
        return fields.fields().stream()
                .filter(field -> label.equals(field.label()))
                .map(Field::value)
                .findFirst()
                .orElseThrow();
    }
}
