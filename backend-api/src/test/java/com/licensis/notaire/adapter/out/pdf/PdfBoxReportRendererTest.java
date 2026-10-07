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
    @DisplayName("shouldRenderTitleFieldsTableAndTotalsWithSpanishText")
    void shouldRenderTitleFieldsTableAndTotalsWithSpanishText() throws IOException {
        ReportDocument document = new ReportDocument("Presupuesto N° 12", "CU45", List.of(
                new Fields("Datos", List.of(new Field("Cliente", "Lucía Peña"), new Field("Trámite", "Compraventa"))),
                new Table("Conceptos", List.of(Column.left("Concepto", 3), Column.right("Valor", 1)),
                        List.of(List.of("Honorarios escribanía", "$ 10.000,00")),
                        "Sin conceptos", List.of(new Field("Total", "$ 10.000,00")))));

        byte[] pdf = renderer.render(document);

        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (PDDocument parsed = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(parsed);
            assertThat(text).contains("Presupuesto N° 12", "CU45", "Datos", "Cliente", "Lucía Peña", "Trámite",
                    "Conceptos", "Concepto", "Valor", "Honorarios escribanía", "$ 10.000,00", "Total",
                    "Página 1 de 1");
            assertThat(parsed.getDocumentInformation().getTitle()).isEqualTo("Presupuesto N° 12");
        }
    }

    @Test
    @DisplayName("shouldShowTheEmptyMessageForATableWithoutRows")
    void shouldShowTheEmptyMessageForATableWithoutRows() throws IOException {
        ReportDocument document = new ReportDocument("Inmuebles", null, List.of(
                new Table("Inmuebles", List.of(Column.left("Nomenclatura", 1)), List.of(),
                        "Sin inmuebles asociados", List.of())));

        assertThat(text(renderer.render(document))).contains("Sin inmuebles asociados");
    }

    @Test
    @DisplayName("shouldBreakLongTablesAcrossPagesRepeatingTheHeader")
    void shouldBreakLongTablesAcrossPagesRepeatingTheHeader() throws IOException {
        List<List<String>> rows = new ArrayList<>();
        for (int i = 1; i <= 120; i++) {
            rows.add(List.of("Fila " + i, "Estado " + i));
        }
        ReportDocument document = new ReportDocument("Historial", null, List.of(
                new Table("Movimientos", List.of(Column.left("Detalle", 1), Column.left("Estado", 1)), rows,
                        "", List.of())));

        try (PDDocument parsed = Loader.loadPDF(renderer.render(document))) {
            int pages = parsed.getNumberOfPages();
            assertThat(pages).isGreaterThan(1);
            String text = new PDFTextStripper().getText(parsed);
            assertThat(text).contains("Fila 1", "Fila 120", "Página 1 de " + pages, "Página " + pages + " de " + pages);
            assertThat(text.split("Detalle", -1)).hasSize(pages + 1);
        }
    }

    @Test
    @DisplayName("shouldWrapLongValuesInsteadOfCuttingThem")
    void shouldWrapLongValuesInsteadOfCuttingThem() throws IOException {
        String longNotes = "Observación " + "muy larga de la gestión ".repeat(20) + "fin-de-texto";
        ReportDocument document = new ReportDocument("Notas", null, List.of(
                new Fields("Detalle", List.of(new Field("Observaciones", longNotes))),
                new Table("Tabla", List.of(Column.left("Nota", 1), Column.right("Monto", 1)),
                        List.of(List.of(longNotes, "1")), "", List.of())));

        String text = text(renderer.render(document));

        assertThat(text.split("fin-de-texto", -1)).hasSize(3);
    }

    @Test
    @DisplayName("shouldReplaceCharactersTheStandardFontCannotEncode")
    void shouldReplaceCharactersTheStandardFontCannotEncode() throws IOException {
        ReportDocument document = new ReportDocument("Símbolos ✓", null, List.of(
                new Fields("Datos", List.of(new Field("Nota", "línea uno\nlínea\tdos 漢 €")))));

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
