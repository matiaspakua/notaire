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
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Assembles the CU24 index-book {@link ReportDocument} from recorded folios.
 */
@Component
public class IndexBookReportBuilder {

    static final String EMPTY_PERIOD = "No deeds were recorded for the requested period.";

    private static final List<Column> COLUMNS = List.of(
            Column.left("Person", 3f),
            Column.left("Procedure", 2.4f),
            Column.right("Deed number", 1.2f),
            Column.right("First folio", 1.2f),
            Column.left("Signing day/month", 1.4f));

    private final FolioRepository folioRepository;
    private final ProcedureRepository procedureRepository;

    public IndexBookReportBuilder(FolioRepository folioRepository, ProcedureRepository procedureRepository) {
        this.folioRepository = folioRepository;
        this.procedureRepository = procedureRepository;
    }

    public ReportDocument build(Integer year, Integer idNotary, Integer idFolioType) {
        int periodYear = year == null ? 0 : year;
        List<Folio> recorded = folioRepository.findByYear(periodYear).stream()
                .filter(folio -> folio.getFkIdDeed() != null)
                .filter(folio -> matchesNotary(folio, idNotary))
                .filter(folio -> matchesProtocol(folio, idFolioType))
                .toList();

        Map<Integer, List<Folio>> foliosByDeed = new LinkedHashMap<>();
        for (Folio folio : recorded) {
            foliosByDeed.computeIfAbsent(folio.getFkIdDeed().getIdDeed(), ignored -> new ArrayList<>()).add(folio);
        }

        List<List<String>> rows = new ArrayList<>();
        for (Map.Entry<Integer, List<Folio>> entry : foliosByDeed.entrySet()) {
            rows.addAll(rowsForDeed(entry.getKey(), entry.getValue()));
        }
        rows.sort(Comparator.comparing(row -> sortKey(row.getFirst())));

        String period = periodLabel(periodYear, idNotary, idFolioType, recorded);
        String empty = rows.isEmpty() ? EMPTY_PERIOD : null;
        List<Field> filters = new ArrayList<>();
        filters.add(new Field("Year", String.valueOf(periodYear)));
        if (idNotary != null) {
            filters.add(new Field("Notary", notaryLabel(idNotary, recorded)));
        }
        if (idFolioType != null) {
            filters.add(new Field("Protocol", protocolLabel(idFolioType, recorded)));
        }

        return new ReportDocument(
                "Index book",
                "CU24",
                List.of(
                        new Fields("Period", filters),
                        new Table("Index", COLUMNS, rows, EMPTY_PERIOD, List.of())),
                new Identity("CU24", "index-book-" + periodYear, period, empty,
                        ReportDocument.PageOrientation.PORTRAIT));
    }

    private List<List<String>> rowsForDeed(Integer deedId, List<Folio> folios) {
        Deed deed = folios.getFirst().getFkIdDeed();
        int firstFolio = folios.stream().mapToInt(Folio::getNumber).min().orElse(0);
        List<Procedure> procedures = procedureRepository.findByFkIdDeedIdDeed(deedId);
        String procedureName = procedures.stream()
                .map(Procedure::getFkIdProcedureType)
                .filter(Objects::nonNull)
                .map(ProcedureType::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse(ReportLocaleFormats.NOT_RECORDED);

        Map<Integer, Person> people = new LinkedHashMap<>();
        for (Procedure procedure : procedures) {
            List<Person> involved = involvedPeople(procedure);
            for (Person person : involved) {
                people.putIfAbsent(personKey(person), person);
            }
        }

        List<List<String>> rows = new ArrayList<>();
        for (Person person : people.values()) {
            rows.add(List.of(
                    ReportDocumentFactory.fullName(person),
                    procedureName,
                    String.valueOf(deed.getNumber()),
                    String.valueOf(firstFolio),
                    ReportLocaleFormats.dayMonth(deed.getDateDeedrecording())));
        }
        return rows;
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

    private static String periodLabel(int year, Integer idNotary, Integer idFolioType, List<Folio> recorded) {
        StringBuilder period = new StringBuilder("Year ").append(year);
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

    private static Integer personKey(Person person) {
        return person.getPersonId() != null ? person.getPersonId() : System.identityHashCode(person);
    }

    static String sortKey(String displayName) {
        if (displayName == null) {
            return "";
        }
        String stripped = Normalizer.normalize(displayName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        String[] parts = stripped.split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        return parts[parts.length - 1] + " " + String.join(" ", List.of(parts).subList(0, parts.length - 1));
    }
}
