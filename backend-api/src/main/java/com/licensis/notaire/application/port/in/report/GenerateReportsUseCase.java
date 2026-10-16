package com.licensis.notaire.application.port.in.report;

/**
 * Inbound port for PDF report generation (epic #1693 / issue #1849).
 * Controllers call this port only; entity access stays in outbound adapters.
 */
public interface GenerateReportsUseCase {

    byte[] generateBudgetReport(Integer idBudget);

    byte[] generateBudgetPropertiesReport(Integer idBudget);

    byte[] generateProcedureDocumentsListReport(String nameTypeProcedure);

    byte[] generateManagementHistoryReport(Integer idManagement);

    byte[] generateDocumentsDueSoonReport(Integer idSubmittedDocument);

    byte[] generateDebtDocumentsReport(Integer numberManagement);

    byte[] generateIndexBookReport(Integer year, Integer idNotary, Integer idFolioType);

    byte[] generateMonthlyTaxDeclarationReport(Integer year, Integer month, Integer idNotary, Integer idFolioType);

    byte[] generateTestimonyCopyReport(Integer idTestimony);

    byte[] generateNotebookCoverReport(Integer idNotebook);

    byte[] generateIncomeTaxDeclarationReport(Integer year, Integer month, Integer idNotary, Integer idFolioType);

    byte[] generateRegistrationDraftReport(Integer idRegistrationDraft);

    byte[] generatePaymentReceiptReport(Integer idPayment);
}
