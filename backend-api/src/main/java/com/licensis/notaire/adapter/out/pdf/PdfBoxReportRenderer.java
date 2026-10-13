package com.licensis.notaire.adapter.out.pdf;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Column;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Identity;
import com.licensis.notaire.application.port.out.report.ReportDocument.Section;
import com.licensis.notaire.application.port.out.report.ReportDocument.Table;
import com.licensis.notaire.application.port.out.report.ReportRenderer;
import com.licensis.notaire.application.usecase.report.ReportLocaleFormats;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * {@link ReportRenderer} on Apache PDFBox using the shared design system (issue #1593).
 */
@Component
public class PdfBoxReportRenderer implements ReportRenderer {

    private static final float LEADING_FACTOR = 1.3f;
    private static final float MAX_LABEL_SHARE = 0.4f;

    private final Clock clock;
    private final ZoneId zone;
    private final String productVersion;
    private final ReportAssetCatalog assets;
    private final ReportGenerationRecorder recorder;

    public PdfBoxReportRenderer() {
        this(Clock.system(ReportTheme.ZONE), ReportTheme.ZONE, "Notaire", new ReportAssetCatalog(), null);
    }

    public PdfBoxReportRenderer(Clock clock, ZoneId zone, String productVersion, ReportAssetCatalog assets) {
        this(clock, zone, productVersion, assets, null);
    }

    @Autowired
    public PdfBoxReportRenderer(Clock clock,
            @Value("${app.version:unknown}") String productVersion,
            ReportAssetCatalog assets,
            ObjectProvider<ReportGenerationRecorder> recorder) {
        this(clock, ReportTheme.ZONE, productVersion, assets, recorder.getIfAvailable());
    }

    PdfBoxReportRenderer(Clock clock, ZoneId zone, String productVersion, ReportAssetCatalog assets,
            ReportGenerationRecorder recorder) {
        this.clock = clock == null ? Clock.system(ReportTheme.ZONE) : clock;
        this.zone = zone == null ? ReportTheme.ZONE : zone;
        this.productVersion = productVersion == null || productVersion.isBlank() ? "Notaire" : productVersion;
        this.assets = assets == null ? new ReportAssetCatalog() : assets;
        this.recorder = recorder;
    }

    @Override
    public byte[] render(ReportDocument document) {
        long started = System.nanoTime();
        String type = document.identity() == null || document.identity().reportType() == null
                ? "report" : document.identity().reportType();
        try (PDDocument pdf = new PDDocument()) {
            applyMetadata(pdf, document);
            PDFont regular = PDType0Font.load(pdf, new ByteArrayInputStream(assets.regularFont()), true);
            PDFont bold = PDType0Font.load(pdf, new ByteArrayInputStream(assets.boldFont()), true);
            try (Layout layout = new Layout(pdf, document, regular, bold, assets.wordmark())) {
                layout.title(document.title(), document.subtitle());
                layout.emptyState(document.identity());
                for (Section section : document.sections()) {
                    if (section instanceof Fields fields) {
                        layout.fields(fields);
                    } else if (section instanceof Table table) {
                        layout.table(table);
                    }
                }
            }
            addFooters(pdf, regular, document);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            byte[] bytes = out.toByteArray();
            if (recorder != null) {
                recorder.recordSuccess(type, System.nanoTime() - started, pdf.getNumberOfPages(), rowCount(document));
            }
            return bytes;
        } catch (IOException e) {
            if (recorder != null) {
                recorder.recordFailure(type);
            }
            throw new UncheckedIOException("Failed to generate the PDF report: " + document.title(), e);
        }
    }

    private void applyMetadata(PDDocument pdf, ReportDocument document) {
        PDDocumentInformation info = pdf.getDocumentInformation();
        info.setTitle(document.title());
        info.setProducer("Notaire");
        info.setCreator("Notaire " + productVersion);
        Calendar created = Calendar.getInstance(TimeZone.getTimeZone(zone));
        created.setTimeInMillis(clock.instant().toEpochMilli());
        info.setCreationDate(created);
        pdf.getDocumentCatalog().setLanguage(ReportTheme.LANGUAGE);
        PDMarkInfo markInfo = new PDMarkInfo();
        markInfo.setMarked(true);
        pdf.getDocumentCatalog().setMarkInfo(markInfo);
    }

    private void addFooters(PDDocument pdf, PDFont font, ReportDocument document) throws IOException {
        boolean landscape = document.identity().landscape();
        float pageWidth = ReportTheme.pageWidthPt(landscape);
        String generated = ReportLocaleFormats.generatedAt(clock.instant(), zone);
        String left = assets.wordmark() + " " + productVersion + " — " + generated;
        int pages = pdf.getNumberOfPages();
        for (int i = 0; i < pages; i++) {
            PDPage page = pdf.getPage(i);
            try (PDPageContentStream stream = new PDPageContentStream(pdf, page, AppendMode.APPEND, true, true)) {
                stream.setNonStrokingColor(ReportTheme.MUTED_GRAY, ReportTheme.MUTED_GRAY, ReportTheme.MUTED_GRAY);
                float y = ReportTheme.MARGIN_BOTTOM_PT / 2f;
                showText(stream, font, ReportTheme.FOOTER_SIZE_PT, ReportTheme.MARGIN_SIDE_PT, y,
                        ReportTextSanitizer.sanitize(font, left));
                String pageLabel = String.format(ReportTheme.PAGE_LABEL_FORMAT, i + 1, pages);
                float width = width(font, ReportTheme.FOOTER_SIZE_PT, pageLabel);
                showText(stream, font, ReportTheme.FOOTER_SIZE_PT,
                        pageWidth - ReportTheme.MARGIN_SIDE_PT - width, y, pageLabel);
            }
        }
    }

    private static int rowCount(ReportDocument document) {
        int rows = 0;
        for (Section section : document.sections()) {
            if (section instanceof Table table) {
                rows += table.rows().size();
            }
        }
        return rows;
    }

    /** Cursor over the pages being written; opens a new page when the current one is full. */
    private static final class Layout implements AutoCloseable {

        private final PDDocument pdf;
        private final ReportDocument document;
        private final PDFont regular;
        private final PDFont bold;
        private final String wordmark;
        private final boolean landscape;
        private final float pageWidth;
        private final float pageHeight;
        private final float contentWidth;
        private final float contentBottom;
        private PDPageContentStream stream;
        private float y;

        Layout(PDDocument pdf, ReportDocument document, PDFont regular, PDFont bold, String wordmark)
                throws IOException {
            this.pdf = pdf;
            this.document = document;
            this.regular = regular;
            this.bold = bold;
            this.wordmark = wordmark;
            this.landscape = document.identity().landscape();
            this.pageWidth = ReportTheme.pageWidthPt(landscape);
            this.pageHeight = ReportTheme.pageHeightPt(landscape);
            this.contentWidth = pageWidth - 2 * ReportTheme.MARGIN_SIDE_PT;
            this.contentBottom = ReportTheme.MARGIN_BOTTOM_PT + ReportTheme.FOOTER_BAND_PT;
            newPage();
        }

        void title(String title, String subtitle) throws IOException {
            writeWrapped(bold, ReportTheme.TITLE_SIZE_PT, title, ReportTheme.MARGIN_SIDE_PT, contentWidth);
            y -= ReportTheme.TITLE_META_GAP_PT;
            if (subtitle != null && !subtitle.isBlank()) {
                stream.setNonStrokingColor(ReportTheme.MUTED_GRAY, ReportTheme.MUTED_GRAY, ReportTheme.MUTED_GRAY);
                writeWrapped(regular, ReportTheme.META_SIZE_PT, subtitle, ReportTheme.MARGIN_SIDE_PT, contentWidth);
                stream.setNonStrokingColor(ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY);
            }
            Identity identity = document.identity();
            if (notBlank(identity.periodOrAggregate())) {
                writeWrapped(regular, ReportTheme.META_SIZE_PT, identity.periodOrAggregate(),
                        ReportTheme.MARGIN_SIDE_PT, contentWidth);
            }
            y -= ReportTheme.GRID_PT;
        }

        void emptyState(Identity identity) throws IOException {
            if (identity == null || !notBlank(identity.emptyStateMessage())) {
                return;
            }
            writeWrapped(regular, ReportTheme.BODY_SIZE_PT, identity.emptyStateMessage(),
                    ReportTheme.MARGIN_SIDE_PT, contentWidth);
            y -= ReportTheme.GRID_PT;
        }

        void fields(Fields section) throws IOException {
            heading(section.heading());
            float labelWidth = 0;
            for (Field field : section.fields()) {
                labelWidth = Math.max(labelWidth, width(bold, ReportTheme.BODY_SIZE_PT,
                        ReportTextSanitizer.sanitize(bold, field.label())));
            }
            labelWidth = Math.min(labelWidth + 2 * ReportTheme.CELL_PADDING_PT, contentWidth * MAX_LABEL_SHARE);
            float leading = ReportTheme.BODY_LEADING_PT;
            for (Field field : section.fields()) {
                List<String> labelLines = wrap(bold, ReportTheme.BODY_SIZE_PT, field.label(),
                        labelWidth - ReportTheme.CELL_PADDING_PT);
                List<String> valueLines = wrap(regular, ReportTheme.BODY_SIZE_PT, field.value(),
                        contentWidth - labelWidth);
                int lines = Math.max(labelLines.size(), valueLines.size());
                ensureSpace(lines * leading);
                float top = y;
                drawLines(bold, ReportTheme.BODY_SIZE_PT, labelLines, ReportTheme.MARGIN_SIDE_PT, top, leading);
                drawLines(regular, ReportTheme.BODY_SIZE_PT, valueLines, ReportTheme.MARGIN_SIDE_PT + labelWidth, top,
                        leading);
                y = top - lines * leading;
            }
            y -= ReportTheme.GRID_PT;
        }

        void table(Table table) throws IOException {
            heading(table.heading());
            float[] widths = columnWidths(table.columns());
            drawHeaderRow(table.columns(), widths);
            if (table.rows().isEmpty()) {
                String message = table.emptyMessage() == null ? "" : table.emptyMessage();
                writeWrapped(regular, ReportTheme.TABLE_SIZE_PT, message,
                        ReportTheme.MARGIN_SIDE_PT + ReportTheme.CELL_PADDING_PT,
                        contentWidth - 2 * ReportTheme.CELL_PADDING_PT);
            }
            for (List<String> row : table.rows()) {
                List<List<String>> cells = new ArrayList<>();
                int lines = 1;
                for (int c = 0; c < widths.length; c++) {
                    List<String> cell = wrap(regular, ReportTheme.TABLE_SIZE_PT, row.get(c),
                            widths[c] - 2 * ReportTheme.CELL_PADDING_PT);
                    cells.add(cell);
                    lines = Math.max(lines, cell.size());
                }
                float rowHeight = lines * ReportTheme.TABLE_SIZE_PT * LEADING_FACTOR
                        + 2 * ReportTheme.CELL_PADDING_PT;
                if (y - rowHeight < contentBottom) {
                    newPage();
                    drawHeaderRow(table.columns(), widths);
                }
                drawRow(regular, table.columns(), widths, cells, rowHeight);
            }
            for (Field total : table.totals()) {
                String text = ReportTextSanitizer.sanitize(bold,
                        total.label() + ": " + (total.value() == null ? "" : total.value()));
                float leading = ReportTheme.BODY_LEADING_PT;
                ensureSpace(leading + ReportTheme.CELL_PADDING_PT);
                y -= ReportTheme.CELL_PADDING_PT;
                showText(stream, bold, ReportTheme.BODY_SIZE_PT,
                        ReportTheme.MARGIN_SIDE_PT + contentWidth - width(bold, ReportTheme.BODY_SIZE_PT, text),
                        y - ReportTheme.BODY_SIZE_PT, text);
                y -= leading;
            }
            y -= ReportTheme.GRID_PT;
        }

        private void heading(String heading) throws IOException {
            if (heading == null || heading.isBlank()) {
                return;
            }
            ensureSpace(ReportTheme.HEADING_SIZE_PT * LEADING_FACTOR + 3 * ReportTheme.TABLE_SIZE_PT * LEADING_FACTOR);
            stream.setNonStrokingColor(ReportTheme.ACCENT_R, ReportTheme.ACCENT_G, ReportTheme.ACCENT_B);
            writeWrapped(bold, ReportTheme.HEADING_SIZE_PT, heading, ReportTheme.MARGIN_SIDE_PT, contentWidth);
            stream.setNonStrokingColor(ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY);
            y -= ReportTheme.CELL_PADDING_PT;
        }

        private void drawHeaderRow(List<Column> columns, float[] widths) throws IOException {
            List<List<String>> cells = new ArrayList<>();
            int lines = 1;
            for (int c = 0; c < widths.length; c++) {
                List<String> cell = wrap(bold, ReportTheme.TABLE_SIZE_PT, columns.get(c).header(),
                        widths[c] - 2 * ReportTheme.CELL_PADDING_PT);
                cells.add(cell);
                lines = Math.max(lines, cell.size());
            }
            float rowHeight = lines * ReportTheme.TABLE_SIZE_PT * LEADING_FACTOR + 2 * ReportTheme.CELL_PADDING_PT;
            ensureSpace(rowHeight + ReportTheme.TABLE_SIZE_PT * LEADING_FACTOR + 2 * ReportTheme.CELL_PADDING_PT);
            stream.setNonStrokingColor(ReportTheme.HEADER_FILL, ReportTheme.HEADER_FILL, ReportTheme.HEADER_FILL);
            stream.addRect(ReportTheme.MARGIN_SIDE_PT, y - rowHeight, contentWidth, rowHeight);
            stream.fill();
            stream.setNonStrokingColor(ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY);
            drawRow(bold, columns, widths, cells, rowHeight);
        }

        private void drawRow(PDFont font, List<Column> columns, float[] widths, List<List<String>> cells,
                float rowHeight) throws IOException {
            float leading = ReportTheme.TABLE_SIZE_PT * LEADING_FACTOR;
            float x = ReportTheme.MARGIN_SIDE_PT;
            for (int c = 0; c < widths.length; c++) {
                float lineTop = y - ReportTheme.CELL_PADDING_PT;
                for (String line : cells.get(c)) {
                    float textX = columns.get(c).alignRight()
                            ? x + widths[c] - ReportTheme.CELL_PADDING_PT - width(font, ReportTheme.TABLE_SIZE_PT, line)
                            : x + ReportTheme.CELL_PADDING_PT;
                    showText(stream, font, ReportTheme.TABLE_SIZE_PT, textX, lineTop - ReportTheme.TABLE_SIZE_PT, line);
                    lineTop -= leading;
                }
                x += widths[c];
            }
            y -= rowHeight;
            stream.setStrokingColor(ReportTheme.RULE_GRAY, ReportTheme.RULE_GRAY, ReportTheme.RULE_GRAY);
            stream.setLineWidth(0.5f);
            stream.moveTo(ReportTheme.MARGIN_SIDE_PT, y);
            stream.lineTo(ReportTheme.MARGIN_SIDE_PT + contentWidth, y);
            stream.stroke();
        }

        private void writeWrapped(PDFont font, float size, String text, float x, float maxWidth) throws IOException {
            float leading = size * LEADING_FACTOR;
            for (String line : wrap(font, size, text, maxWidth)) {
                ensureSpace(leading);
                showText(stream, font, size, x, y - size, line);
                y -= leading;
            }
        }

        private void drawLines(PDFont font, float size, List<String> lines, float x, float top, float leading)
                throws IOException {
            float lineTop = top;
            for (String line : lines) {
                showText(stream, font, size, x, lineTop - size, line);
                lineTop -= leading;
            }
        }

        private void ensureSpace(float height) throws IOException {
            if (y - height < contentBottom) {
                newPage();
            }
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
            pdf.addPage(page);
            stream = new PDPageContentStream(pdf, page);
            stream.setNonStrokingColor(ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY);
            y = pageHeight - ReportTheme.MARGIN_TOP_PT;
            drawRepeatingHeader();
        }

        private void drawRepeatingHeader() throws IOException {
            stream.setNonStrokingColor(ReportTheme.ACCENT_R, ReportTheme.ACCENT_G, ReportTheme.ACCENT_B);
            showText(stream, bold, ReportTheme.WORDMARK_SIZE_PT, ReportTheme.MARGIN_SIDE_PT,
                    y - ReportTheme.WORDMARK_SIZE_PT, ReportTextSanitizer.sanitize(bold, wordmark));
            Identity identity = document.identity();
            String right = joinChrome(identity.reportType(), identity.documentIdentifier());
            if (notBlank(right)) {
                String sanitized = ReportTextSanitizer.sanitize(regular, right);
                float width = width(regular, ReportTheme.META_SIZE_PT, sanitized);
                showText(stream, regular, ReportTheme.META_SIZE_PT,
                        pageWidth - ReportTheme.MARGIN_SIDE_PT - width, y - ReportTheme.WORDMARK_SIZE_PT, sanitized);
            }
            y -= ReportTheme.HEADER_BAND_PT;
            stream.setStrokingColor(ReportTheme.ACCENT_R, ReportTheme.ACCENT_G, ReportTheme.ACCENT_B);
            stream.setLineWidth(1f);
            stream.moveTo(ReportTheme.MARGIN_SIDE_PT, y);
            stream.lineTo(pageWidth - ReportTheme.MARGIN_SIDE_PT, y);
            stream.stroke();
            stream.setNonStrokingColor(ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY, ReportTheme.TEXT_GRAY);
            y -= ReportTheme.GRID_PT;
        }

        private float[] columnWidths(List<Column> columns) {
            float totalWeight = 0;
            for (Column column : columns) {
                totalWeight += column.weight();
            }
            float[] widths = new float[columns.size()];
            for (int c = 0; c < widths.length; c++) {
                widths[c] = contentWidth * columns.get(c).weight() / totalWeight;
            }
            return widths;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }

    private static String joinChrome(String left, String right) {
        if (notBlank(left) && notBlank(right)) {
            return left + " · " + right;
        }
        return notBlank(left) ? left : (notBlank(right) ? right : "");
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static void showText(PDPageContentStream stream, PDFont font, float size, float x, float y, String text)
            throws IOException {
        if (text == null || text.isEmpty()) {
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
    static List<String> wrap(PDFont font, float size, String text, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : ReportTextSanitizer.sanitize(font, text).split(" ")) {
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

    private static float width(PDFont font, float size, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        return font.getStringWidth(text) / 1000f * size;
    }
}
