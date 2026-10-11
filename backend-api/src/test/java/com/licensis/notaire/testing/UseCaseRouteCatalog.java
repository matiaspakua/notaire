package com.licensis.notaire.testing;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public final class UseCaseRouteCatalog {

    private UseCaseRouteCatalog() {
    }

    public record UseCaseRoute(String useCaseId, String useCaseName, RequestMethod method, String pathPattern) {
    }

    public static List<UseCaseRoute> all() {
        return List.of(
                new UseCaseRoute("CU01", "Prepare a budget", RequestMethod.POST, "/api/v1/budgets"),
                new UseCaseRoute("CU02", "Start Management", RequestMethod.POST, "/api/v1/managements"),
                new UseCaseRoute("CU03", "List required documents and certificates", RequestMethod.GET, "/api/v1/reports/procedure-document-list"),
                new UseCaseRoute("CU04", "Record documentation client", RequestMethod.POST, "/api/v1/submitted-documents"),
                new UseCaseRoute("CU05", "Prepare a deed", RequestMethod.POST, "/api/v1/deeds"),
                new UseCaseRoute("CU06", "Sign a deed", RequestMethod.PUT, "/api/v1/deeds/{id}"),
                new UseCaseRoute("CU07", "Generate a testimony", RequestMethod.POST, "/api/v1/testimonies"),
                new UseCaseRoute("CU08", "Verify a testimony", RequestMethod.GET, "/api/v1/testimonies"),
                new UseCaseRoute("CU09", "Record client document debts", RequestMethod.GET, "/api/v1/reports/document-debt"),
                new UseCaseRoute("CU10", "Record external-entity document movements", RequestMethod.POST, "/api/v1/testimony-movements"),
                new UseCaseRoute("CU11", "Enter for registration", RequestMethod.PUT, "/api/v1/testimonies/{id}"),
                new UseCaseRoute("CU12", "Withdraw a testimony", RequestMethod.POST, "/api/v1/copies"),
                new UseCaseRoute("CU13", "View management history", RequestMethod.GET, "/api/v1/history"),
                new UseCaseRoute("CU14", "Query status management", RequestMethod.GET, "/api/v1/management-statuses"),
                new UseCaseRoute("CU15", "Process a payment", RequestMethod.POST, "/api/v1/payments"),
                new UseCaseRoute("CU16", "Archive a management", RequestMethod.PUT, "/api/v1/managements/{id}"),
                new UseCaseRoute("CU17", "Register a person", RequestMethod.POST, "/api/v1/people"),
                new UseCaseRoute("CU18", "Register a client", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU19", "Search managements of a Client", RequestMethod.GET, "/api/v1/managements"),
                new UseCaseRoute("CU20", "Create user", RequestMethod.POST, "/api/v1/users"),
                new UseCaseRoute("CU21", "Update a user account", RequestMethod.PUT, "/api/v1/users/{id}"),
                new UseCaseRoute("CU22", "Register a substitution", RequestMethod.POST, "/api/v1/substitutions"),
                new UseCaseRoute("CU23", "View user activity log", RequestMethod.GET, "/api/v1/audit-log/user/{idUser}"),
                new UseCaseRoute("CU24", "Generate the index book", RequestMethod.GET, "/api/v1/reports/index-book"),
                new UseCaseRoute("CU25", "Generate DDJJ of the month", RequestMethod.GET, "/api/v1/reports/monthly-sworn-statement"),
                new UseCaseRoute("CU26", "Enter new type of procedure", RequestMethod.POST, "/api/v1/procedure-types"),
                new UseCaseRoute("CU27", "Enter new type of document", RequestMethod.POST, "/api/v1/document-types"),
                new UseCaseRoute("CU28", "Enter new folios", RequestMethod.POST, "/api/v1/folios"),
                new UseCaseRoute("CU29", "Enter new concept", RequestMethod.POST, "/api/v1/concepts"),
                new UseCaseRoute("CU30", "Enter new status of Management", RequestMethod.POST, "/api/v1/management-statuses"),
                new UseCaseRoute("CU31", "Update type of procedure", RequestMethod.PUT, "/api/v1/procedure-types/{id}"),
                new UseCaseRoute("CU32", "Update a document type", RequestMethod.PUT, "/api/v1/document-types/{id}"),
                new UseCaseRoute("CU33", "Update a folio", RequestMethod.PUT, "/api/v1/folios/{id}"),
                new UseCaseRoute("CU34", "Update concept", RequestMethod.PUT, "/api/v1/concepts/{id}"),
                new UseCaseRoute("CU35", "Update status of Management", RequestMethod.PUT, "/api/v1/management-statuses/{id}"),
                new UseCaseRoute("CU36", "Enter folio types", RequestMethod.POST, "/api/v1/folio-types"),
                new UseCaseRoute("CU37", "Delete concept", RequestMethod.DELETE, "/api/v1/concepts/{id}"),
                new UseCaseRoute("CU38", "Delete type of document", RequestMethod.DELETE, "/api/v1/document-types/{id}"),
                new UseCaseRoute("CU39", "Create Template Budget", RequestMethod.POST, "/api/v1/budget-templates"),
                new UseCaseRoute("CU40", "Update a folio type", RequestMethod.PUT, "/api/v1/folio-types/{id}"),
                new UseCaseRoute("CU41", "Update a client", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU42", "Report upcoming expirations", RequestMethod.GET, "/api/v1/reports/documents-due/{idSubmittedDocument}"),
                new UseCaseRoute("CU43", "Reenter documentation", RequestMethod.PUT, "/api/v1/submitted-documents/{id}"),
                new UseCaseRoute("CU44", "Reenter a testimony", RequestMethod.PUT, "/api/v1/testimonies/{id}"),
                new UseCaseRoute("CU45", "Update a budget", RequestMethod.PUT, "/api/v1/budgets/{id}"),
                new UseCaseRoute("CU46", "View client details", RequestMethod.GET, "/api/v1/people/{id}"),
                new UseCaseRoute("CU47", "Consultar Payment", RequestMethod.GET, "/api/v1/payments/budget/{idBudget}"),
                new UseCaseRoute("CU48", "Register a notary", RequestMethod.POST, "/api/v1/people"),
                new UseCaseRoute("CU49", "Delete Template Budget", RequestMethod.DELETE, "/api/v1/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}"),
                new UseCaseRoute("CU50", "Generate the tax sworn statement", RequestMethod.GET, "/api/v1/reports/tax-sworn-statement"),
                new UseCaseRoute("CU51", "Update a notary", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU52", "Update a deed", RequestMethod.PUT, "/api/v1/deeds/{id}"),
                new UseCaseRoute("CU53", "Update Management", RequestMethod.PUT, "/api/v1/managements/{id}"),
                new UseCaseRoute("CU54", "Update a person", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU55", "Update Template Budget", RequestMethod.PUT, "/api/v1/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}"),
                new UseCaseRoute("CU56", "Record registration", RequestMethod.PUT, "/api/v1/testimonies/{id}"),
                new UseCaseRoute("CU57", "Delete type of procedure", RequestMethod.DELETE, "/api/v1/procedure-types/{id}"),
                new UseCaseRoute("CU58", "Delete type of folio", RequestMethod.DELETE, "/api/v1/folio-types/{id}"),
                new UseCaseRoute("CU59", "Look up substitutions", RequestMethod.GET, "/api/v1/substitutions"),
                new UseCaseRoute("CU60", "Search Budget", RequestMethod.GET, "/api/v1/budgets"),
                new UseCaseRoute("CU61", "Search person or client", RequestMethod.GET, "/api/v1/people/search"),
                new UseCaseRoute("CU62", "Search Deed", RequestMethod.GET, "/api/v1/deeds"),
                new UseCaseRoute("CU63", "Search Folios", RequestMethod.GET, "/api/v1/folios"),
                new UseCaseRoute("CU64", "Search Procedure type", RequestMethod.GET, "/api/v1/procedure-types"),
                new UseCaseRoute("CU65", "Search document types", RequestMethod.GET, "/api/v1/document-types"),
                new UseCaseRoute("CU66", "Search Concepts", RequestMethod.GET, "/api/v1/concepts"),
                new UseCaseRoute("CU67", "Search Management statuses", RequestMethod.GET, "/api/v1/management-statuses"),
                new UseCaseRoute("CU68", "Search folio types", RequestMethod.GET, "/api/v1/folio-types"),
                new UseCaseRoute("CU83", "Manage Workflow", RequestMethod.POST, "/api/v1/workflow-definition"),
                new UseCaseRoute("CU83", "Manage Transitions Workflow", RequestMethod.POST, "/api/v1/workflow-transition"),
                new UseCaseRoute("CU83", "Validate workflow consistency", RequestMethod.POST, "/api/v1/workflow-definition/{id}/validate"),
                new UseCaseRoute("CU83", "Assign a workflow to a procedure type", RequestMethod.PUT, "/api/v1/procedure-types/{id}/workflow")
        );
    }
}
