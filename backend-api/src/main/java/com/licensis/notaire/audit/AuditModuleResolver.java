package com.licensis.notaire.audit;

import java.util.Map;

/**
 * Translates a controller class name (e.g. {@code EscrituraController}) into a
 * user-facing module name (e.g. {@code Escrituras}) used in the
 * {@code registro_auditoria.modulo} column.
 *
 * <p>Centralising the mapping in a single helper avoids scattering the
 * controller-to-module translation across the audit aspect and keeps the
 * Spanish, plural, business-friendly names consistent.
 */
public final class AuditModuleResolver {

    private static final Map<String, String> ControllerTOMODULE = Map.ofEntries(
            Map.entry("DeedController", "Deeds"),
            Map.entry("PersonController", "People"),
            Map.entry("BudgetController", "Budgets"),
            Map.entry("ConceptController", "Concepts"),
            Map.entry("UserController", "Users"),
            Map.entry("PaymentController", "Payments"),
            Map.entry("ProcedureController", "Procedures"),
            Map.entry("CopyController", "Copies"),
            Map.entry("TestimonyController", "Testimonies"),
            Map.entry("FolioController", "Folios"),
            Map.entry("ItemController", "Items"),
            Map.entry("PropertyController", "Properties"),
            Map.entry("HistoryController", "History"),
            Map.entry("SubmittedDocumentController", "Documents"),
            Map.entry("ManagementController", "Managements"),
            Map.entry("ManagementStatusController", "ManagementStatuses"),
            Map.entry("TestimonyMovementController", "TestimonyMovements"),
            Map.entry("SubstitutionController", "Substitutions"),
            Map.entry("BudgetTemplateController", "BudgetTemplates"),
            Map.entry("ProcedureTemplateController", "ProcedureTemplates"),
            Map.entry("DocumentTypeController", "DocumentTypes"),
            Map.entry("FolioTypeController", "FolioTypes"),
            Map.entry("ProcedureTypeController", "ProcedureTypes"),
            Map.entry("IdentificationTypeController", "IdentificationTypes"),
            Map.entry("ReportController", "Reports")
    );

    private static final String ControllerSUFFIX = "Controller";
    private static final String DEFAULT_MODULE = "General";

    private AuditModuleResolver() {
        // Utility class.
    }

    /**
     * Resolve a friendly module name for the given controller class.
     *
     * @param controllerSimpleName simple class name (e.g. {@code EscrituraController})
     * @return business-friendly module label, never {@code null}
     */
    public static String resolve(String controllerSimpleName) {
        if (controllerSimpleName == null || controllerSimpleName.isBlank()) {
            return DEFAULT_MODULE;
        }
        String mapped = ControllerTOMODULE.get(controllerSimpleName);
        if (mapped != null) {
            return mapped;
        }
        if (controllerSimpleName.endsWith(ControllerSUFFIX)) {
            return controllerSimpleName.substring(0,
                    controllerSimpleName.length() - ControllerSUFFIX.length());
        }
        return controllerSimpleName;
    }
}
