package com.licensis.notaire.application.port.out.report;

/**
 * Outbound port that assembles the CU82 registration-draft {@link ReportDocument}.
 */
public interface RegistrationDraftReportPort {

    ReportDocument build(Integer idRegistrationDraft);
}
