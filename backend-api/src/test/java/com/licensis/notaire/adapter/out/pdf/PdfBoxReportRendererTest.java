package com.licensis.notaire.adapter.out.pdf;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Column;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Table;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The PDFBox adapter renders the framework-free {@link ReportDocument} that replaced
 * the JasperReports templates (issue #567).
 */
@DisplayName("PdfBoxReportRenderer (issue #567)")
class PdfBoxReportRendererTest {

    private final PdfBoxReportRenderer renderer = new PdfBoxReportRenderer();

    @Test
    @DisplayName("shouldRenderTitleFieldsTableAndTotals")
    void shouldRenderTitleFieldsTableAndTotals() throws IOException {
        ReportDocument document = new ReportDocument("Budget N° 12", "CU45", List.of(
                new Fields("Details", List.of(new Field("Client", "Lucía Peña"), new Field("Procedure", "Compraventa"))),
                new Table("Concepts", List.of(Column.left("Concept", 3), Column.right("Amount", 1)),
                        List.of(List.of("Notarial fees", "$ 10.000,00")),
                        "No concepts", List.of(new Field("Total", "$ 10.000,00")))));

        byte[] pdf = renderer.render(document);

        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (PDDocument parsed = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(parsed);
            assertThat(text).contains("Budget N° 12", "CU45", "Details", "Client", "Lucía Peña", "Procedure",
                    "Concepts", "Concept", "Amount", "Notarial fees", "$ 10.000,00", "Total",
                    "Page 1 of 1");
            assertThat(parsed.getDocumentInformation().getTitle()).isEqualTo("Budget N° 12");
        }
    }

    @Test
    @DisplayName("shouldShowTheEmptyMessageForATableWithoutRows")
    void shouldShowTheEmptyMessageForATableWithoutRows() throws IOException {
        ReportDocument document = new ReportDocument("Properties", null, List.of(
                new Table("Properties", List.of(Column.left("Designation", 1)), List.of(),
                        "No linked properties", List.of())));

        assertThat(text(renderer.render(document))).contains("No linked properties");
    }

    @Test
    @DisplayName("shouldBreakLongTablesAcrossPagesRepeatingTheHeader")
    void shouldBreakLongTablesAcrossPagesRepeatingTheHeader() throws IOException {
        List<List<String>> rows = new ArrayList<>();
        for (int i = 1; i <= 120; i++) {
            rows.add(List.of("Row " + i, "Status " + i));
        }
        ReportDocument document = new ReportDocument("History", null, List.of(
                new Table("Movements", List.of(Column.left("Detail", 1), Column.left("Status", 1)), rows,
                        "", List.of())));

        try (PDDocument parsed = Loader.loadPDF(renderer.render(document))) {
            int pages = parsed.getNumberOfPages();
            assertThat(pages).isGreaterThan(1);
            String text = new PDFTextStripper().getText(parsed);
            assertThat(text).contains("Row 1", "Row 120", "Page 1 of " + pages, "Page " + pages + " of " + pages);
            assertThat(text.split("Detail", -1)).hasSize(pages + 1);
        }
    }

    @Test
    @DisplayName("shouldWrapLongValuesInsteadOfCuttingThem")
    void shouldWrapLongValuesInsteadOfCuttingThem() throws IOException {
        String longNotes = "Observation " + "very long management note ".repeat(20) + "end-of-text";
        ReportDocument document = new ReportDocument("Notes", null, List.of(
                new Fields("Detail", List.of(new Field("Notes", longNotes))),
                new Table("Table", List.of(Column.left("Note", 1), Column.right("Amount", 1)),
                        List.of(List.of(longNotes, "1")), "", List.of())));

        String text = text(renderer.render(document));

        assertThat(text.split("end-of-text", -1)).hasSize(3);
    }

    @Test
    @DisplayName("shouldReplaceCharactersTheStandardFontCannotEncode")
    void shouldReplaceCharactersTheStandardFontCannotEncode() throws IOException {
        ReportDocument document = new ReportDocument("Símbolos ✓", null, List.of(
                new Fields("Details", List.of(new Field("Note", "línea uno\nlínea\tdos 漢 €")))));

        String text = text(renderer.render(document));

        assertThat(text).contains("Símbolos ?", "línea uno línea dos ? €");
    }

    @Test
    @DisplayName("shouldRejectARowThatDoesNotMatchTheColumns")
    void shouldRejectARowThatDoesNotMatchTheColumns() {
        assertThatThrownBy(() -> new Table("T", List.of(Column.left("A", 1)), List.of(List.of("a", "b")), "",
                List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String text(byte[] pdf) throws IOException {
        try (PDDocument parsed = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(parsed);
        }
    }
}
