package com.licensis.notaire.adapter.out.report;

import com.licensis.notaire.application.port.out.report.RegistrationDraftReportPort;
import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Identity;
import com.licensis.notaire.application.usecase.report.ReportLocaleFormats;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.RegistrationDraftRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Assembles the CU82 registration-draft {@link ReportDocument} from recorded fields.
 */
@Component
public class RegistrationDraftReportAdapter implements RegistrationDraftReportPort {

    private final RegistrationDraftRepository registrationDraftRepository;
    private final ProcedureRepository procedureRepository;

    public RegistrationDraftReportAdapter(RegistrationDraftRepository registrationDraftRepository,
                                          ProcedureRepository procedureRepository) {
        this.registrationDraftRepository = registrationDraftRepository;
        this.procedureRepository = procedureRepository;
    }

    @Override
    public ReportDocument build(Integer idRegistrationDraft) {
        RegistrationDraft draft = registrationDraftRepository.findById(idRegistrationDraft)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration draft not found with ID: " + idRegistrationDraft));

        Deed deed = draft.getFkIdDeed();
        Integer deedId = deed == null ? null : deed.getIdDeed();
        List<Procedure> procedures = deedId == null
                ? List.of()
                : procedureRepository.findByFkIdDeedIdDeed(deedId);
        Property property = procedures.stream()
                .map(Procedure::getFkIdProperty)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        List<Field> fields = new ArrayList<>();
        fields.add(new Field("Draft", String.valueOf(draft.getNumber())));
        fields.add(new Field("Status", ReportLocaleFormats.fallback(draft.getStatus())));
        fields.add(new Field("Generated", ReportLocaleFormats.day(draft.getDateGeneration())));
        fields.add(new Field("Price", ReportLocaleFormats.money(draft.getOperationPrice())));
        fields.add(new Field("Deed", deed == null
                ? ReportLocaleFormats.NOT_RECORDED
                : String.valueOf(deed.getNumber())));
        fields.add(new Field("Deed date", deed == null
                ? ReportLocaleFormats.NOT_RECORDED
                : ReportLocaleFormats.day(deed.getDateDeedrecording())));
        fields.add(new Field("Parties", parties(procedures)));
        fields.add(new Field("Cadastral", ReportLocaleFormats.fallback(
                property == null ? null : property.getCadastralDesignation())));
        fields.add(new Field("Registration", ReportLocaleFormats.fallback(
                property == null ? null : property.getRegistrationNumber())));
        fields.add(new Field("Volume/folio", ReportLocaleFormats.fallback(
                property == null ? null : property.getVolumeFolioLandRecord())));
        fields.add(new Field("Boundaries", ReportLocaleFormats.fallback(
                property == null ? null : property.getBoundaries())));
        fields.add(new Field("Fiscal appraisal", ReportLocaleFormats.money(
                property == null ? null : property.getFiscalAppraisal())));
        fields.add(new Field("Address", ReportLocaleFormats.fallback(
                property == null ? null : property.getAddress())));
        fields.add(new Field("Notes", ReportLocaleFormats.fallback(draft.getRegistryNotes())));

        return new ReportDocument(
                "Registration draft",
                "CU82",
                List.of(new Fields("Registration draft", fields)),
                new Identity("CU82", "registration-draft-" + draft.getNumber(),
                        "Draft " + draft.getNumber(), null, ReportDocument.PageOrientation.PORTRAIT));
    }

    private static String parties(List<Procedure> procedures) {
        return procedures.stream()
                .flatMap(procedure -> involvedPeople(procedure).stream())
                .map(RegistrationDraftReportAdapter::fullName)
                .filter(name -> !ReportLocaleFormats.NOT_RECORDED.equals(name))
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse(ReportLocaleFormats.NOT_RECORDED);
    }

    private static List<Person> involvedPeople(Procedure procedure) {
        List<Person> listed = procedure.getPersonList() == null ? List.of() : procedure.getPersonList().stream()
                .filter(Objects::nonNull)
                .toList();
        if (!listed.isEmpty()) {
            return listed;
        }
        Budget budget = procedure.getFkIdBudget();
        Person client = budget == null ? null : budget.getFkIdPerson();
        return client == null ? List.of() : List.of(client);
    }

    private static String fullName(Person person) {
        if (person == null) {
            return ReportLocaleFormats.NOT_RECORDED;
        }
        String name = (Objects.toString(person.getFirstName(), "") + " "
                + Objects.toString(person.getLastName(), "")).trim();
        return name.isEmpty() ? ReportLocaleFormats.NOT_RECORDED : name;
    }
}
