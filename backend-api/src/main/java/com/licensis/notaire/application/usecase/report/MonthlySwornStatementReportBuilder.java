package com.licensis.notaire.application.usecase.report;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Column;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Identity;
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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Assembles the CU25 monthly sworn-statement {@link ReportDocument} from recorded folios.
 */
@Component
public class MonthlySwornStatementReportBuilder {

    static final String EMPTY_PERIOD = "No deeds were recorded for the requested period.";

    private static final List<Column> COLUMNS = List.of(
            Column.right("Deed", 0.9f),
            Column.right("First folio", 1.0f),
            Column.right("Day", 0.7f),
            Column.left("Acts and parties", 2.6f),
            Column.left("Annex", 0.8f),
            Column.left("Cadastral", 1.6f),
            Column.right("Fiscal appraisal", 1.4f),
            Column.right("Price", 1.3f),
            Column.right("Tax", 0.8f));

    private final FolioRepository folioRepository;
    private final ProcedureRepository procedureRepository;
    private final SubstitutionRepository substitutionRepository;
    private final RegistrationDraftRepository registrationDraftRepository;

    public MonthlySwornStatementReportBuilder(FolioRepository folioRepository,
                                              ProcedureRepository procedureRepository,
                                              SubstitutionRepository substitutionRepository,
                                              RegistrationDraftRepository registrationDraftRepository) {
        this.folioRepository = folioRepository;
        this.procedureRepository = procedureRepository;
        this.substitutionRepository = substitutionRepository;
        this.registrationDraftRepository = registrationDraftRepository;
    }

    public ReportDocument build(Integer year, Integer month, Integer idNotary, Integer idFolioType) {
        int periodYear = year == null ? 0 : year;
        int periodMonth = month == null ? 0 : month;
        List<Folio> recorded = folioRepository.findByYear(periodYear).stream()
                .filter(folio -> folio.getFkIdDeed() != null)
                .filter(folio -> matchesMonth(folio, periodYear, periodMonth))
                .filter(folio -> matchesNotary(folio, idNotary))
                .filter(folio -> matchesProtocol(folio, idFolioType))
                .toList();

        Map<Integer, List<Folio>> foliosByDeed = new LinkedHashMap<>();
        for (Folio folio : recorded) {
            foliosByDeed.computeIfAbsent(folio.getFkIdDeed().getIdDeed(), ignored -> new ArrayList<>()).add(folio);
        }

        List<List<String>> rows = new ArrayList<>();
        BigDecimal fiscalTotal = null;
        BigDecimal priceTotal = null;
        for (Map.Entry<Integer, List<Folio>> entry : foliosByDeed.entrySet()) {
            RowValues values = rowForDeed(entry.getKey(), entry.getValue());
            rows.add(values.cells());
            fiscalTotal = sumRecorded(fiscalTotal, values.fiscal());
            priceTotal = sumRecorded(priceTotal, values.price());
        }
        rows.sort(Comparator.comparing(row -> row.getFirst()));

        Person titular = resolveTitular(idNotary, recorded);
        List<Field> header = new ArrayList<>();
        header.add(new Field("Year", String.valueOf(periodYear)));
        header.add(new Field("Month", String.valueOf(periodMonth)));
        header.add(new Field("Titular", ReportDocumentFactory.fullName(titular)));
        header.add(new Field("Registration", registrationLabel(titular)));
        header.add(new Field("Substitute", substituteLabel(titular, periodYear, periodMonth)));
        if (idNotary != null) {
            header.add(new Field("Notary", notaryLabel(idNotary, recorded)));
        }
        if (idFolioType != null) {
            header.add(new Field("Protocol", protocolLabel(idFolioType, recorded)));
        }

        List<Field> totals = List.of(
                new Field("Fiscal appraisal", ReportLocaleFormats.money(fiscalTotal)),
                new Field("Price", ReportLocaleFormats.money(priceTotal)));
        String empty = rows.isEmpty() ? EMPTY_PERIOD : null;
        String period = periodLabel(periodYear, periodMonth, idNotary, idFolioType, recorded);

        return new ReportDocument(
                "Monthly sworn statement",
                "CU25",
                List.of(
                        new Fields("Period", header),
                        new Table("Deeds", COLUMNS, rows, EMPTY_PERIOD, totals)),
                new Identity("CU25", "monthly-sworn-" + periodYear + "-" + periodMonth, period, empty,
                        ReportDocument.PageOrientation.LANDSCAPE));
    }

    private RowValues rowForDeed(Integer deedId, List<Folio> folios) {
        Deed deed = folios.getFirst().getFkIdDeed();
        int firstFolio = folios.stream().mapToInt(Folio::getNumber).min().orElse(0);
        List<Procedure> procedures = procedureRepository.findByFkIdDeedIdDeed(deedId);
        boolean principal = folios.stream().anyMatch(folio -> !isAuxiliary(folio.getFkIdFolioType()));

        String acts = procedures.stream()
                .map(Procedure::getFkIdProcedureType)
                .filter(Objects::nonNull)
                .map(ProcedureType::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse(ReportLocaleFormats.NOT_RECORDED);
        String parties = procedures.stream()
                .flatMap(procedure -> involvedPeople(procedure).stream())
                .map(ReportDocumentFactory::fullName)
                .filter(name -> !ReportLocaleFormats.NOT_RECORDED.equals(name))
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse(ReportLocaleFormats.NOT_RECORDED);
        String actsAndParties = ReportLocaleFormats.NOT_RECORDED.equals(parties)
                ? acts
                : acts + " — " + parties;

        Property property = principal ? procedures.stream()
                .map(Procedure::getFkIdProperty)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null) : null;
        BigDecimal fiscal = property == null ? null : property.getFiscalAppraisal();
        BigDecimal price = registrationDraftRepository.findByFkIdDeedIdDeed(deedId)
                .map(RegistrationDraft::getOperationPrice)
                .orElse(null);

        return new RowValues(List.of(
                String.valueOf(deed.getNumber()),
                String.valueOf(firstFolio),
                ReportLocaleFormats.dayOfMonth(deed.getDateDeedrecording()),
                actsAndParties,
                ReportLocaleFormats.NOT_RECORDED,
                ReportLocaleFormats.fallback(property == null ? null : property.getCadastralDesignation()),
                ReportLocaleFormats.money(fiscal),
                ReportLocaleFormats.money(price),
                ReportLocaleFormats.NOT_RECORDED), fiscal, price);
    }

    private String substituteLabel(Person titular, int year, int month) {
        if (titular == null || titular.getPersonId() == null || year <= 0 || month < 1 || month > 12) {
            return ReportLocaleFormats.NOT_RECORDED;
        }
        Date midMonth = java.sql.Date.valueOf(YearMonth.of(year, month).atDay(Math.min(15, YearMonth.of(year, month).lengthOfMonth())));
        List<Substitution> covering = substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        titular.getPersonId(), midMonth, midMonth);
        if (covering.isEmpty()) {
            return ReportLocaleFormats.NOT_RECORDED;
        }
        return ReportDocumentFactory.fullName(covering.getFirst().getFkIdSubstitute());
    }

    private static Person resolveTitular(Integer idNotary, List<Folio> recorded) {
        if (idNotary != null) {
            return recorded.stream()
                    .map(Folio::getFkIdNotaryPerson)
                    .filter(Objects::nonNull)
                    .filter(person -> idNotary.equals(person.getPersonId()))
                    .findFirst()
                    .orElse(null);
        }
        Map<Integer, Person> distinct = new LinkedHashMap<>();
        for (Folio folio : recorded) {
            Person notary = folio.getFkIdNotaryPerson();
            if (notary != null && notary.getPersonId() != null) {
                distinct.putIfAbsent(notary.getPersonId(), notary);
            }
        }
        return distinct.size() == 1 ? distinct.values().iterator().next() : null;
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

    private static boolean matchesMonth(Folio folio, int year, int month) {
        LocalDate date = ReportLocaleFormats.localDate(folio.getFkIdDeed().getDateDeedrecording());
        return date != null && date.getYear() == year && date.getMonthValue() == month;
    }

    private static boolean matchesNotary(Folio folio, Integer idNotary) {
        if (idNotary == null) {
            return true;
        }
        Person notary = folio.getFkIdNotaryPerson();
        return notary != null && idNotary.equals(notary.getPersonId());
    }

    private static boolean matchesProtocol(Folio folio, Integer idFolioType) {
        if (idFolioType == null) {
            return true;
        }
        FolioType type = folio.getFkIdFolioType();
        return type != null && idFolioType.equals(type.getIdFolioType());
    }

    private static boolean isAuxiliary(FolioType type) {
        return type != null && type.isIsAuxiliary();
    }

    private static String periodLabel(int year, int month, Integer idNotary, Integer idFolioType, List<Folio> recorded) {
        StringBuilder period = new StringBuilder().append(month).append('/').append(year);
        if (idNotary != null) {
            period.append(" · ").append(notaryLabel(idNotary, recorded));
        }
        if (idFolioType != null) {
            period.append(" · ").append(protocolLabel(idFolioType, recorded));
        }
        return period.toString();
    }

    private static String notaryLabel(Integer idNotary, List<Folio> recorded) {
        return recorded.stream()
                .map(Folio::getFkIdNotaryPerson)
                .filter(Objects::nonNull)
                .filter(person -> idNotary.equals(person.getPersonId()))
                .findFirst()
                .map(person -> person.getNotaryRegistrationNumber() == null
                        ? String.valueOf(idNotary)
                        : String.valueOf(person.getNotaryRegistrationNumber()))
                .orElse(String.valueOf(idNotary));
    }

    private static String protocolLabel(Integer idFolioType, List<Folio> recorded) {
        return recorded.stream()
                .map(Folio::getFkIdFolioType)
                .filter(Objects::nonNull)
                .filter(type -> idFolioType.equals(type.getIdFolioType()))
                .map(FolioType::getName)
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElse(String.valueOf(idFolioType));
    }

    private static String registrationLabel(Person titular) {
        if (titular == null || titular.getNotaryRegistrationNumber() == null) {
            return ReportLocaleFormats.NOT_RECORDED;
        }
        return String.valueOf(titular.getNotaryRegistrationNumber());
    }

    private static BigDecimal sumRecorded(BigDecimal total, BigDecimal value) {
        if (value == null) {
            return total;
        }
        return total == null ? value : total.add(value);
    }

    private record RowValues(List<String> cells, BigDecimal fiscal, BigDecimal price) {
    }
}
