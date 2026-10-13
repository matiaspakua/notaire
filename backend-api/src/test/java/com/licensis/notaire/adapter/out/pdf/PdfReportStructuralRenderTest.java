package com.licensis.notaire.adapter.out.pdf;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Column;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Table;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PDF structural raster (issue #1593)")
class PdfReportStructuralRenderTest {

    private static final float DPI = 144f;

    @Test
    @DisplayName("shouldKeepInkInsideA4SafeZones")
    void shouldKeepInkInsideA4SafeZones() throws IOException {
        ReportDocument document = new ReportDocument("Budget N° 12", "CU01", List.of(
                new Fields("Details", List.of(new Field("Client", "Lucía Peña"))),
                new Table("Concepts", List.of(Column.left("Concept", 3), Column.right("Amount", 1)),
                        List.of(List.of("Notarial fees", "$ 10.000,00")),
                        "No concepts", List.of(new Field("Total", "$ 10.000,00")))));

        byte[] pdf = new PdfBoxReportRenderer().render(document);
        try (PDDocument parsed = Loader.loadPDF(pdf)) {
            BufferedImage image = new PDFRenderer(parsed).renderImageWithDPI(0, DPI);
            int expectedWidth = Math.round(210f / 25.4f * DPI);
            int expectedHeight = Math.round(297f / 25.4f * DPI);
            assertThat(image.getWidth()).isCloseTo(expectedWidth, org.assertj.core.data.Offset.offset(2));
            assertThat(image.getHeight()).isCloseTo(expectedHeight, org.assertj.core.data.Offset.offset(2));

            int margin = Math.round(18f / 25.4f * DPI);
            int footer = Math.round(16f / 25.4f * DPI);
            assertThat(hasInk(image, margin, 0, image.getWidth() - margin, margin + 20)).isTrue();
            assertThat(hasInk(image, margin, image.getHeight() - footer, image.getWidth() - margin, image.getHeight()))
                    .isTrue();
            assertThat(hasInk(image, margin, margin + 30, image.getWidth() - margin, image.getHeight() - footer - 10))
                    .isTrue();
            assertThat(inkRatio(image, 0, 0, Math.max(1, margin - 4), image.getHeight())).isLessThan(0.02);
        }
    }

    private static boolean hasInk(BufferedImage image, int x0, int y0, int x1, int y1) {
        return inkRatio(image, x0, y0, x1, y1) > 0.001;
    }

    private static double inkRatio(BufferedImage image, int x0, int y0, int x1, int y1) {
        int left = Math.max(0, Math.min(x0, x1));
        int right = Math.min(image.getWidth(), Math.max(x0, x1));
        int top = Math.max(0, Math.min(y0, y1));
        int bottom = Math.min(image.getHeight(), Math.max(y0, y1));
        if (right <= left || bottom <= top) {
            return 0;
        }
        int ink = 0;
        int samples = 0;
        for (int y = top; y < bottom; y += 2) {
            for (int x = left; x < right; x += 2) {
                samples++;
                int rgb = image.getRGB(x, y) & 0x00FFFFFF;
                if (rgb < 0xF2F2F2) {
                    ink++;
                }
            }
        }
        return samples == 0 ? 0 : (double) ink / samples;
    }
}
