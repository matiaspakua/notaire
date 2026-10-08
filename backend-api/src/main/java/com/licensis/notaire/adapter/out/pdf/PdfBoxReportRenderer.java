package com.licensis.notaire.adapter.out.pdf;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Column;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Section;
import com.licensis.notaire.application.port.out.report.ReportDocument.Table;
import com.licensis.notaire.application.port.out.report.ReportRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * {@link ReportRenderer} on Apache PDFBox (Apache License 2.0), replacing the
 * JasperReports 3.5.3 templates (issue #567).
 *
 * <p>Renders A4 pages with the standard Helvetica font (WinAnsi encoding, which covers
 * Spanish text). Long values wrap, tables break across pages repeating their header,
 * and every page gets a footer with the generation time and "Página i de n".
 * Characters the standard font cannot encode are replaced with {@code ?} rather than
 * failing the whole report.
 */
@Component
public class PdfBoxReportRenderer implements ReportRenderer {

    private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
    private static final float MARGIN = 50f;
    private static final float FOOTER_ZONE = 40f;
    private static final float CONTENT_WIDTH = PAGE_SIZE.getWidth() - 2 * MARGIN;
    private static final float TITLE_SIZE = 16f;
    private static final float SUBTITLE_SIZE = 10f;
    private static final float HEADING_SIZE = 12f;
    private static final float BODY_SIZE = 10f;
    private static final float TABLE_SIZE = 9f;
    private static final float FOOTER_SIZE = 8f;
    private static final float LEADING_FACTOR = 1.3f;
    private static final float CELL_PADDING = 4f;
    private static final float MAX_LABEL_SHARE = 0.4f;
    private static final float GRAY = 0.4f;
    private static final float HEADER_FILL = 0.9f;
    private static final float RULE_GRAY = 0.75f;
    private static final DateTimeFormatter GENERATED_AT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] render(ReportDocument document) {
        try (PDDocument pdf = new PDDocument()) {
            PDDocumentInformation info = pdf.getDocumentInformation();
            info.setTitle(document.title());
            info.setProducer("Notaire");
            info.setCreationDate(Calendar.getInstance());

            try (Layout layout = new Layout(pdf)) {
                layout.title(document.title(), document.subtitle());
                for (Section section : document.sections()) {
                    if (section instanceof Fields fields) {
                        layout.fields(fields);
                    } else if (section instanceof Table table) {
                        layout.table(table);
                    }
                }
            }
            addFooters(pdf, footerText());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error al generar el reporte PDF: " + document.title(), e);
        }
    }

    private static String footerText() {
        return "Notaire — generado el " + LocalDateTime.now().format(GENERATED_AT);
    }

    private static void addFooters(PDDocument pdf, String footerText) throws IOException {
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        int pages = pdf.getNumberOfPages();
        for (int i = 0; i < pages; i++) {
            PDPage page = pdf.getPage(i);
            try (PDPageContentStream stream = new PDPageContentStream(pdf, page, AppendMode.APPEND, true, true)) {
                stream.setNonStrokingColor(GRAY, GRAY, GRAY);
                float y = MARGIN / 2;
                showText(stream, font, FOOTER_SIZE, MARGIN, y, sanitize(font, footerText));
                String pageLabel = "Página " + (i + 1) + " de " + pages;
                float width = width(font, FOOTER_SIZE, pageLabel);
                showText(stream, font, FOOTER_SIZE, PAGE_SIZE.getWidth() - MARGIN - width, y, pageLabel);
            }
        }
    }

    /** Cursor over the pages being written; opens a new page when the current one is full. */
    private static final class Layout implements AutoCloseable {

        private final PDDocument pdf;
        private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;

        Layout(PDDocument pdf) throws IOException {
            this.pdf = pdf;
            newPage();
        }

        void title(String title, String subtitle) throws IOException {
            writeWrapped(bold, TITLE_SIZE, title, MARGIN, CONTENT_WIDTH);
            if (subtitle != null && !subtitle.isBlank()) {
                stream.setNonStrokingColor(GRAY, GRAY, GRAY);
                writeWrapped(regular, SUBTITLE_SIZE, subtitle, MARGIN, CONTENT_WIDTH);
                stream.setNonStrokingColor(0f, 0f, 0f);
            }
            y -= BODY_SIZE;
        }

        void fields(Fields section) throws IOException {
            heading(section.heading());
            float labelWidth = 0;
            for (Field field : section.fields()) {
                labelWidth = Math.max(labelWidth, width(bold, BODY_SIZE, sanitize(bold, field.label())));
            }
            labelWidth = Math.min(labelWidth + 2 * CELL_PADDING, CONTENT_WIDTH * MAX_LABEL_SHARE);
            float leading = BODY_SIZE * LEADING_FACTOR;
            for (Field field : section.fields()) {
                List<String> labelLines = wrap(bold, BODY_SIZE, field.label(), labelWidth - CELL_PADDING);
                List<String> valueLines = wrap(regular, BODY_SIZE, field.value(), CONTENT_WIDTH - labelWidth);
                int lines = Math.max(labelLines.size(), valueLines.size());
                ensureSpace(lines * leading);
                float top = y;
                drawLines(bold, BODY_SIZE, labelLines, MARGIN, top, leading);
                drawLines(regular, BODY_SIZE, valueLines, MARGIN + labelWidth, top, leading);
                y = top - lines * leading;
            }
            y -= BODY_SIZE;
        }

        void table(Table table) throws IOException {
            heading(table.heading());
            float[] widths = columnWidths(table.columns());
            drawHeaderRow(table.columns(), widths);
            if (table.rows().isEmpty()) {
                String message = table.emptyMessage() == null ? "" : table.emptyMessage();
                writeWrapped(regular, TABLE_SIZE, message, MARGIN + CELL_PADDING, CONTENT_WIDTH - 2 * CELL_PADDING);
            }
            for (List<String> row : table.rows()) {
                List<List<String>> cells = new ArrayList<>();
                int lines = 1;
                for (int c = 0; c < widths.length; c++) {
                    List<String> cell = wrap(regular, TABLE_SIZE, row.get(c), widths[c] - 2 * CELL_PADDING);
                    cells.add(cell);
                    lines = Math.max(lines, cell.size());
                }
                float rowHeight = lines * TABLE_SIZE * LEADING_FACTOR + 2 * CELL_PADDING;
                if (y - rowHeight < MARGIN + FOOTER_ZONE) {
                    newPage();
                    drawHeaderRow(table.columns(), widths);
                }
                drawRow(regular, table.columns(), widths, cells, rowHeight);
            }
            for (Field total : table.totals()) {
                String text = sanitize(bold, total.label() + ": " + (total.value() == null ? "" : total.value()));
                float leading = BODY_SIZE * LEADING_FACTOR;
                ensureSpace(leading + CELL_PADDING);
                y -= CELL_PADDING;
                showText(stream, bold, BODY_SIZE, MARGIN + CONTENT_WIDTH - width(bold, BODY_SIZE, text),
                        y - BODY_SIZE, text);
                y -= leading;
            }
            y -= BODY_SIZE;
        }

        private void heading(String heading) throws IOException {
            if (heading == null || heading.isBlank()) {
                return;
            }
            // Keep a heading together with at least a couple of lines of its content.
            ensureSpace(HEADING_SIZE * LEADING_FACTOR + 3 * TABLE_SIZE * LEADING_FACTOR);
            writeWrapped(bold, HEADING_SIZE, heading, MARGIN, CONTENT_WIDTH);
            y -= CELL_PADDING;
        }

        private void drawHeaderRow(List<Column> columns, float[] widths) throws IOException {
            List<List<String>> cells = new ArrayList<>();
            int lines = 1;
            for (int c = 0; c < widths.length; c++) {
                List<String> cell = wrap(bold, TABLE_SIZE, columns.get(c).header(), widths[c] - 2 * CELL_PADDING);
                cells.add(cell);
                lines = Math.max(lines, cell.size());
            }
            float rowHeight = lines * TABLE_SIZE * LEADING_FACTOR + 2 * CELL_PADDING;
            ensureSpace(rowHeight + TABLE_SIZE * LEADING_FACTOR + 2 * CELL_PADDING);
            stream.setNonStrokingColor(HEADER_FILL, HEADER_FILL, HEADER_FILL);
            stream.addRect(MARGIN, y - rowHeight, CONTENT_WIDTH, rowHeight);
            stream.fill();
            stream.setNonStrokingColor(0f, 0f, 0f);
            drawRow(bold, columns, widths, cells, rowHeight);
        }

        private void drawRow(PDType1Font font, List<Column> columns, float[] widths, List<List<String>> cells,
                float rowHeight) throws IOException {
            float leading = TABLE_SIZE * LEADING_FACTOR;
            float x = MARGIN;
            for (int c = 0; c < widths.length; c++) {
                float lineTop = y - CELL_PADDING;
                for (String line : cells.get(c)) {
                    float textX = columns.get(c).alignRight()
                            ? x + widths[c] - CELL_PADDING - width(font, TABLE_SIZE, line)
                            : x + CELL_PADDING;
                    showText(stream, font, TABLE_SIZE, textX, lineTop - TABLE_SIZE, line);
                    lineTop -= leading;
                }
                x += widths[c];
            }
            y -= rowHeight;
            stream.setStrokingColor(RULE_GRAY, RULE_GRAY, RULE_GRAY);
            stream.setLineWidth(0.5f);
            stream.moveTo(MARGIN, y);
            stream.lineTo(MARGIN + CONTENT_WIDTH, y);
            stream.stroke();
        }

        private void writeWrapped(PDType1Font font, float size, String text, float x, float maxWidth)
                throws IOException {
            float leading = size * LEADING_FACTOR;
            for (String line : wrap(font, size, text, maxWidth)) {
                ensureSpace(leading);
                showText(stream, font, size, x, y - size, line);
                y -= leading;
            }
        }

        private void drawLines(PDType1Font font, float size, List<String> lines, float x, float top, float leading)
                throws IOException {
            float lineTop = top;
            for (String line : lines) {
                showText(stream, font, size, x, lineTop - size, line);
                lineTop -= leading;
            }
        }

        private void ensureSpace(float height) throws IOException {
            if (y - height < MARGIN + FOOTER_ZONE) {
                newPage();
            }
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(PAGE_SIZE);
            pdf.addPage(page);
            stream = new PDPageContentStream(pdf, page);
            y = PAGE_SIZE.getHeight() - MARGIN;
        }

        private static float[] columnWidths(List<Column> columns) {
            float totalWeight = 0;
            for (Column column : columns) {
                totalWeight += column.weight();
            }
            float[] widths = new float[columns.size()];
            for (int c = 0; c < widths.length; c++) {
                widths[c] = CONTENT_WIDTH * columns.get(c).weight() / totalWeight;
            }
            return widths;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }

    private static void showText(PDPageContentStream stream, PDType1Font font, float size, float x, float y,
            String text) throws IOException {
        if (text.isEmpty()) {
            return;
        }
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    /**
     * Splits text into lines no wider than {@code maxWidth}, breaking at spaces and, for a
     * single word wider than a line, inside the word. Always returns at least one line.
     */
    static List<String> wrap(PDType1Font font, float size, String text, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : sanitize(font, text).split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (width(font, size, candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
                continue;
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
                line.setLength(0);
            }
            String rest = word;
            while (width(font, size, rest) > maxWidth && rest.length() > 1) {
                int cut = rest.length() - 1;
                while (cut > 1 && width(font, size, rest.substring(0, cut)) > maxWidth) {
                    cut--;
                }
                lines.add(rest.substring(0, cut));
                rest = rest.substring(cut);
            }
            line.append(rest);
        }
        if (!line.isEmpty() || lines.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    /**
     * Makes text printable with a standard 14 font: control characters become spaces and
     * characters outside its encoding become {@code ?}.
     */
    static String sanitize(PDType1Font font, String text) {
        if (text == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(text.length());
        text.codePoints().forEach(codePoint -> {
            if (Character.isISOControl(codePoint) || Character.isWhitespace(codePoint)) {
                out.append(' ');
                return;
            }
            String character = new String(Character.toChars(codePoint));
            try {
                font.encode(character);
                out.append(character);
            } catch (IOException | IllegalArgumentException e) {
                out.append('?');
            }
        });
        return out.toString().replaceAll(" {2,}", " ").trim();
    }

    private static float width(PDType1Font font, float size, String text) throws IOException {
        return font.getStringWidth(text) / 1000f * size;
    }
}
