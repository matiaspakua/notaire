package com.licensis.notaire.application.port.out.report;

import java.util.List;
import java.util.Objects;

/**
 * Framework-free description of a printable report: a title, an optional subtitle
 * (typically the use case it serves) and an ordered list of sections.
 *
 * <p>Use cases build a {@code ReportDocument} from the domain model and hand it to a
 * {@link ReportRenderer}; they never depend on a PDF library. It replaced the
 * JasperReports templates that queried the legacy schema (issue #567).
 *
 * @param title    report title, shown on the first page and stored as PDF metadata
 * @param subtitle optional line under the title, {@code null} when absent
 * @param sections sections in display order
 */
public record ReportDocument(String title, String subtitle, List<Section> sections) {

    public ReportDocument {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("A report needs a title");
        }
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    /** A block of the report. */
    public sealed interface Section permits Fields, Table {
    }

    /**
     * A labelled value.
     *
     * @param label label shown before the value
     * @param value value; {@code null} is rendered as an empty string
     */
    public record Field(String label, String value) {

        public Field {
            Objects.requireNonNull(label, "label");
        }
    }

    /**
     * Label/value pairs, e.g. the header data of a budget.
     *
     * @param heading optional section heading, {@code null} when absent
     * @param fields  pairs in display order
     */
    public record Fields(String heading, List<Field> fields) implements Section {

        public Fields {
            fields = fields == null ? List.of() : List.copyOf(fields);
        }
    }

    /**
     * A table column.
     *
     * @param header     column header
     * @param weight     relative width; the table spreads the page width by weight
     * @param alignRight whether cell text is right-aligned (amounts, counts)
     */
    public record Column(String header, float weight, boolean alignRight) {

        public Column {
            Objects.requireNonNull(header, "header");
            if (weight <= 0) {
                throw new IllegalArgumentException("A column weight must be positive");
            }
        }

        public static Column left(String header, float weight) {
            return new Column(header, weight, false);
        }

        public static Column right(String header, float weight) {
            return new Column(header, weight, true);
        }
    }

    /**
     * Rows of values under column headers, with optional totals below.
     *
     * @param heading      optional section heading, {@code null} when absent
     * @param columns      at least one column
     * @param rows         rows, each with exactly one value per column
     * @param emptyMessage text shown instead of rows when there are none
     * @param totals       label/value pairs shown under the table
     */
    public record Table(String heading, List<Column> columns, List<List<String>> rows, String emptyMessage,
            List<Field> totals) implements Section {

        public Table {
            if (columns == null || columns.isEmpty()) {
                throw new IllegalArgumentException("A table needs at least one column");
            }
            columns = List.copyOf(columns);
            rows = rows == null ? List.of() : rows.stream().map(Table::copyRow).toList();
            for (List<String> row : rows) {
                if (row.size() != columns.size()) {
                    throw new IllegalArgumentException(
                            "Row has " + row.size() + " values for " + columns.size() + " columns");
                }
            }
            totals = totals == null ? List.of() : List.copyOf(totals);
        }

        // List.copyOf rejects null values; a missing cell value is rendered empty.
        private static List<String> copyRow(List<String> row) {
            return row.stream().map(value -> value == null ? "" : value).toList();
        }
    }
}
