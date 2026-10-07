package com.licensis.notaire.application.port.out.report;

/**
 * Outbound port that turns a {@link ReportDocument} into a printable PDF.
 */
public interface ReportRenderer {

    /**
     * Renders the document.
     *
     * @param document the report to render
     * @return the PDF bytes
     */
    byte[] render(ReportDocument document);
}
