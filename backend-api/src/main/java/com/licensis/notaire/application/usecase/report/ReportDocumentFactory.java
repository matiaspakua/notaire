package com.licensis.notaire.application.usecase.report;

import com.licensis.notaire.application.port.in.payment.BudgetSummary;
import com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase;
import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Column;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Section;
import com.licensis.notaire.application.port.out.report.ReportDocument.Table;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Builds the {@link ReportDocument}s of the six reports that used to be JasperReports
 * templates over the legacy schema (issue #567), reading the current domain model.
 *
 * <p>Each method loads one aggregate inside a read-only transaction and fails with
 * {@link ResourceNotFoundException} (HTTP 404) when it does not exist. Budget totals come
 * from the CU47 budget summary so the report and the payment screens agree.
 */
@Component
@Transactional(readOnly = true)
public class ReportDocumentFactory {

    static final String NOT_RECORDED = "N/A";
    private static final String PAYMENT_PENDING = "Pago pendiente";
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale SPANISH_ARGENTINA = Locale.of("es", "AR");

    private final BudgetRepository budgetRepository;
    private final ProcedureRepository procedureRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final ProcedureTemplateRepository procedureTemplateRepository;
    private final DeedManagementRepository deedManagementRepository;
    private final HistoryRepository historyRepository;
    private final SubmittedDocumentRepository submittedDocumentRepository;
    private final GetBudgetSummaryUseCase budgetSummary;

    public ReportDocumentFactory(BudgetRepository budgetRepository, ProcedureRepository procedureRepository,
            ProcedureTypeRepository procedureTypeRepository,
            ProcedureTemplateRepository procedureTemplateRepository,
            DeedManagementRepository deedManagementRepository, HistoryRepository historyRepository,
            SubmittedDocumentRepository submittedDocumentRepository, GetBudgetSummaryUseCase budgetSummary) {
        this.budgetRepository = budgetRepository;
        this.procedureRepository = procedureRepository;
        this.procedureTypeRepository = procedureTypeRepository;
        this.procedureTemplateRepository = procedureTemplateRepository;
        this.deedManagementRepository = deedManagementRepository;
        this.historyRepository = historyRepository;
        this.submittedDocumentRepository = submittedDocumentRepository;
        this.budgetSummary = budgetSummary;
    }

    /**
     * Budget with its charge lines, total and pending balance (CU01); with
     * {@code withProperties} it also lists the properties of the budget's procedures.
     */
    public ReportDocument budget(Integer idBudget, boolean withProperties) {
        Budget budget = budgetRepository.findById(idBudget)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + idBudget));
        List<Procedure> procedures = procedureRepository.findByFkIdBudgetIdBudget(idBudget);
        BudgetSummary summary = budgetSummary.summary(idBudget);

        List<Field> fields = new ArrayList<>(List.of(
                new Field("Número de presupuesto", String.valueOf(budget.getNumber())),
                new Field("Fecha de emisión", day(budget.getDate())),
                new Field("Cliente", fullName(budget.getFkIdPerson())),
                new Field("Encabezado", orNotRecorded(budget.getEncabezado())),
                new Field("Trámite", joinedOr(procedureTypeNames(procedures), "Sin trámites asociados")),
                new Field("Gestión", summary.managementNumber() == null
                        ? "Sin gestión asignada"
                        : summary.managementNumber() + " - " + orNotRecorded(summary.managementHeading())),
                new Field("Estado", orNotRecorded(budget.getStatus()))));
        if (withProperties) {
            fields.add(new Field("Monto del inmueble", money(budget.getPropertyAmount())));
        }

        // Same item source as the CU47 total (BudgetLookupAdapter), so the lines add up to it.
        List<Item> items = budget.getItemList() == null ? List.of() : budget.getItemList();
        List<List<String>> itemRows = items.stream()
                .sorted(Comparator.comparing(Item::getIdItem, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(item -> List.of(
                        orNotRecorded(item.getName()),
                        item.getType() == TypeItem.DESCUENTO ? "Descuento" : "Cargo",
                        money(item.getValue()),
                        item.getPercentage() == null ? "-" : item.getPercentage() + " %"))
                .toList();

        List<Section> sections = new ArrayList<>();
        sections.add(new Fields("Datos del presupuesto", fields));
        if (withProperties) {
            sections.add(new Table("Inmuebles",
                    List.of(Column.left("Nomenclatura catastral", 2), Column.left("Domicilio", 3),
                            Column.right("Valuación fiscal", 1.5f)),
                    distinct(procedures.stream().map(Procedure::getFkIdProperty).toList(), Property::getIdProperty)
                            .stream()
                            .map(property -> List.of(
                                    orNotRecorded(property.getCadastralDesignation()),
                                    orNotRecorded(property.getAddress()),
                                    money(property.getFiscalAppraisal())))
                            .toList(),
                    "Sin inmuebles asociados", List.of()));
        }
        sections.add(new Table("Conceptos",
                List.of(Column.left("Concepto", 3), Column.left("Tipo", 1), Column.right("Valor", 1.4f),
                        Column.right("Porcentaje", 1)),
                itemRows, "Sin conceptos cargados",
                List.of(new Field("Total", money(summary.total())),
                        new Field("Saldo pendiente", money(summary.pendingBalance())))));

        return new ReportDocument("Presupuesto N° " + budget.getNumber(),
                withProperties ? "Presupuesto con inmuebles (CU01)" : "Presupuesto (CU01)", sections);
    }

    /** Documents a procedure type requires, from its procedure template (CU03). */
    public ReportDocument procedureDocuments(String procedureTypeName) {
        ProcedureType type = procedureTypeRepository.findFirstByNameOrderByIdProcedureTypeAsc(procedureTypeName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de trámite no encontrado: " + procedureTypeName));

        List<List<String>> rows = procedureTemplateRepository
                .findByProcedureTypeIdProcedureType(type.getIdProcedureType()).stream()
                .map(ProcedureTemplate::getDocumentType)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(DocumentType::getName, Comparator.nullsLast(String::compareTo)))
                .map(document -> List.of(
                        orNotRecorded(document.getName()),
                        yesNo(document.getExpires()),
                        document.getDueDays() == null ? NOT_RECORDED : String.valueOf(document.getDueDays()),
                        orNotRecorded(document.getDeliveredBy())))
                .toList();

        return new ReportDocument("Documentos por trámite: " + type.getName(), "Listado de documentos (CU03)",
                List.of(
                        new Fields("Tipo de trámite", List.of(
                                new Field("Tipo de trámite", type.getName()),
                                new Field("Se archiva", yesNo(type.getIsArchived())),
                                new Field("Se inscribe", yesNo(type.getIsRegistered())),
                                new Field("Asocia inmuebles", yesNo(type.getAssociatesProperties())),
                                new Field("Observaciones", orNotRecorded(type.getNotes())))),
                        new Table("Documentos requeridos",
                                List.of(Column.left("Tipo de documento", 3), Column.left("Vence", 1),
                                        Column.right("Días de validez", 1.2f), Column.left("Entrega", 2)),
                                rows, "El tipo de trámite no tiene documentos en su plantilla", List.of())));
    }

    /** State history of a management, oldest first (CU13). */
    public ReportDocument managementHistory(Integer idManagement) {
        DeedManagement management = deedManagementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idManagement));
        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(idManagement);

        List<List<String>> rows = historyRepository.findByFkIdManagementIdManagement(idManagement).stream()
                .sorted(Comparator.comparing(History::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(History::getIdHistory, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(entry -> List.of(
                        day(entry.getDate()),
                        entry.getFkIdManagementStatus() == null
                                ? NOT_RECORDED : orNotRecorded(entry.getFkIdManagementStatus().getName()),
                        orNotRecorded(entry.getNotes())))
                .toList();

        return new ReportDocument("Historial de Gestión N° " + management.getNumber(), "Historial de gestión (CU13)",
                List.of(
                        new Fields("Datos de la gestión", List.of(
                                new Field("Gestión", String.valueOf(management.getNumber())),
                                new Field("Encabezado", orNotRecorded(management.getEncabezado())),
                                new Field("Estado actual", management.getFkIdManagementStatus() == null
                                        ? NOT_RECORDED : management.getFkIdManagementStatus().getName()),
                                new Field("Fecha de inicio", day(management.getDateStart())),
                                new Field("Escribano", fullName(management.getFkIdNotaryPerson())),
                                new Field("Clientes de referencia",
                                        joinedOr(clientNames(procedures), NOT_RECORDED)),
                                new Field("Trámites", joinedOr(procedureTypeNames(procedures), "Sin trámites")),
                                new Field("Observaciones", orNotRecorded(management.getNotes())))),
                        new Table("Historial de estados",
                                List.of(Column.left("Fecha", 1), Column.left("Estado", 1.5f),
                                        Column.left("Observaciones", 4)),
                                rows, "Sin movimientos registrados", List.of())));
    }

    /** Expiry, payment and management context of one submitted document (CU42). */
    public ReportDocument submittedDocumentExpiry(Integer idSubmittedDocument) {
        SubmittedDocument document = submittedDocumentRepository.findById(idSubmittedDocument)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento presentado no encontrado con ID: " + idSubmittedDocument));
        Procedure procedure = document.getFkIdProcedure();
        DeedManagement management = procedure == null ? null : procedure.getFkIdManagement();
        Person client = procedure == null || procedure.getFkIdBudget() == null
                ? null : procedure.getFkIdBudget().getFkIdPerson();

        return new ReportDocument("Vencimiento de documento presentado", "Documentos por vencer (CU42)", List.of(
                new Fields("Documento", List.of(
                        new Field("Documento", orNotRecorded(document.getName())),
                        new Field("Tipo de documento", document.getDocumentType() == null
                                ? NOT_RECORDED : orNotRecorded(document.getDocumentType().getName())),
                        new Field("Entrega", orNotRecorded(document.getDeliveredBy())),
                        new Field("Preparado", yesNo(document.getPrepared())),
                        new Field("Observado", yesNo(Boolean.TRUE.equals(document.getFlagged()))),
                        new Field("Número de cartón", document.getCardNumber() == null
                                ? NOT_RECORDED : String.valueOf(document.getCardNumber())),
                        new Field("Fecha de ingreso", day(document.getDateEntry())),
                        new Field("Fecha de salida", day(document.getDateExit())))),
                new Fields("Vencimiento", List.of(
                        new Field("Vence", yesNo(document.getExpires())),
                        new Field("Fecha de vencimiento", day(document.getDateDue())),
                        new Field("Estado", expiryStatus(document.getDateDue())),
                        new Field("Fecha de liberación", day(document.getDateReleased())))),
                new Fields("Pago", List.of(
                        new Field("Importe a pagar", document.getAmountToPay() == null
                                ? "No registra" : money(document.getAmountToPay())),
                        new Field("Fecha de pago", paymentDate(document)))),
                new Fields("Gestión", List.of(
                        new Field("Gestión N°", management == null ? NOT_RECORDED : String.valueOf(management.getNumber())),
                        new Field("Encabezado", management == null ? NOT_RECORDED : orNotRecorded(management.getEncabezado())),
                        new Field("Trámite", procedure == null || procedure.getFkIdProcedureType() == null
                                ? NOT_RECORDED : procedure.getFkIdProcedureType().getName()),
                        new Field("Cliente de referencia", fullName(client))))));
    }

    /** Amounts owed for the documents submitted under a management number (CU09). */
    public ReportDocument documentDebt(Integer managementNumber) {
        List<DeedManagement> managements = deedManagementRepository.findAllByNumber(managementNumber);
        if (managements.isEmpty()) {
            throw new ResourceNotFoundException("Gestión no encontrada con número: " + managementNumber);
        }

        List<List<String>> rows = new ArrayList<>();
        BigDecimal owed = BigDecimal.ZERO;
        for (DeedManagement management : sortedById(managements)) {
            List<SubmittedDocument> documents = submittedDocumentRepository
                    .findByFkIdProcedureFkIdManagementIdManagement(management.getIdManagement());
            for (SubmittedDocument document : documents) {
                Procedure procedure = document.getFkIdProcedure();
                rows.add(List.of(
                        procedure == null || procedure.getFkIdProcedureType() == null
                                ? NOT_RECORDED : procedure.getFkIdProcedureType().getName(),
                        orNotRecorded(document.getName()),
                        document.getAmountToPay() == null ? "No registra" : money(document.getAmountToPay()),
                        paymentDate(document)));
                if (document.getAmountToPay() != null && document.getDatePayment() == null) {
                    owed = owed.add(document.getAmountToPay());
                }
            }
        }

        return new ReportDocument("Deuda de documentos - Gestión N° " + managementNumber,
                "Consulta de deuda de documentos (CU09)", List.of(
                        new Fields("Gestión", List.of(
                                new Field("Gestión N°", String.valueOf(managementNumber)),
                                new Field("Encabezado", joinedOr(managements.stream()
                                        .map(DeedManagement::getEncabezado).filter(Objects::nonNull)
                                        .distinct().toList(), NOT_RECORDED)))),
                        new Table("Documentos",
                                List.of(Column.left("Trámite", 2), Column.left("Documento", 2.5f),
                                        Column.right("Importe", 1.3f), Column.left("Fecha de pago", 1.3f)),
                                rows, "La gestión no tiene documentos presentados",
                                List.of(new Field("Total adeudado", money(owed))))));
    }

    private static List<DeedManagement> sortedById(List<DeedManagement> managements) {
        return managements.stream()
                .sorted(Comparator.comparing(DeedManagement::getIdManagement,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private static String paymentDate(SubmittedDocument document) {
        if (document.getDatePayment() != null) {
            return day(document.getDatePayment());
        }
        boolean owes = document.getAmountToPay() != null && document.getAmountToPay().signum() > 0;
        return owes ? PAYMENT_PENDING : NOT_RECORDED;
    }

    static String expiryStatus(Date dateDue) {
        if (dateDue == null) {
            return "Sin vencimiento registrado";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), localDate(dateDue));
        if (days < 0) {
            return "Vencido hace " + (-days) + (days == -1 ? " día" : " días");
        }
        if (days == 0) {
            return "Vence hoy";
        }
        return "Vence en " + days + (days == 1 ? " día" : " días");
    }

    private static List<String> procedureTypeNames(List<Procedure> procedures) {
        return procedures.stream()
                .map(Procedure::getFkIdProcedureType)
                .filter(Objects::nonNull)
                .map(ProcedureType::getName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private static List<String> clientNames(List<Procedure> procedures) {
        return procedures.stream()
                .map(Procedure::getFkIdBudget)
                .filter(Objects::nonNull)
                .map(Budget::getFkIdPerson)
                .filter(Objects::nonNull)
                .map(ReportDocumentFactory::fullName)
                .distinct()
                .toList();
    }

    private static <T, K> List<T> distinct(List<T> values, Function<T, K> key) {
        Map<K, T> byKey = new LinkedHashMap<>();
        for (T value : values) {
            if (value != null) {
                byKey.putIfAbsent(key.apply(value), value);
            }
        }
        return List.copyOf(byKey.values());
    }

    private static String joinedOr(List<String> values, String fallback) {
        return values.isEmpty() ? fallback : String.join(", ", values);
    }

    static String fullName(Person person) {
        if (person == null) {
            return NOT_RECORDED;
        }
        String name = (Objects.toString(person.getFirstName(), "") + " "
                + Objects.toString(person.getLastName(), "")).trim();
        return name.isEmpty() ? NOT_RECORDED : name;
    }

    static String orNotRecorded(String value) {
        return value == null || value.isBlank() ? NOT_RECORDED : value;
    }

    static String yesNo(boolean value) {
        return value ? "Sí" : "No";
    }

    static String day(Date date) {
        return date == null ? NOT_RECORDED : DAY.format(localDate(date));
    }

    /** {@code java.sql.Date} does not support {@code toInstant()}; both kinds are mapped here. */
    private static LocalDate localDate(Date date) {
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Amounts in the Argentine format used across the UI: {@code $ 1.234,56}. */
    static String money(BigDecimal amount) {
        if (amount == null) {
            return NOT_RECORDED;
        }
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(SPANISH_ARGENTINA));
        return "$ " + format.format(amount);
    }
}
