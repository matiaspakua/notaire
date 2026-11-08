package com.licensis.notaire.application.port.out.report;

/**
 * Outbound port that assembles the CU50 tax sworn-statement {@link ReportDocument}.
 */
public interface TaxSwornStatementReportPort {

    String EMPTY_PERIOD = "No deeds were recorded for the requested period.";

    ReportDocument build(Integer year, Integer month, Integer idNotary, Integer idFolioType);
}
