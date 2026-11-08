package com.licensis.notaire.application.port.out.report;

/**
 * Outbound port that assembles the CU08 testimony-copy {@link ReportDocument}.
 */
public interface TestimonyCopyReportPort {

    ReportDocument build(Integer idTestimony);
}
