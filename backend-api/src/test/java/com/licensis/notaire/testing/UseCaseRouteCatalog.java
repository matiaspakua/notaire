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
                new UseCaseRoute("CU01", "Preparar Budget", RequestMethod.POST, "/api/v1/budgets"),
                new UseCaseRoute("CU02", "Start Management", RequestMethod.POST, "/api/v1/managements"),
                new UseCaseRoute("CU03", "Lista documents y certificados necesarios", RequestMethod.GET, "/api/v1/reports/procedure-document-list"),
                new UseCaseRoute("CU04", "Record documentation client", RequestMethod.POST, "/api/v1/submitted-documents"),
                new UseCaseRoute("CU05", "Preparar deed", RequestMethod.POST, "/api/v1/deeds"),
                new UseCaseRoute("CU06", "Firmar deed", RequestMethod.PUT, "/api/v1/deeds/{id}"),
                new UseCaseRoute("CU07", "Generar testimony", RequestMethod.POST, "/api/v1/testimonies"),
                new UseCaseRoute("CU08", "Verificar Testimony", RequestMethod.GET, "/api/v1/testimonies"),
                new UseCaseRoute("CU09", "Registrar deudas documents de Cliente", RequestMethod.GET, "/api/v1/reports/document-debt"),
                new UseCaseRoute("CU10", "Record movimientos documentation entidades", RequestMethod.POST, "/api/v1/testimony-movements"),
                new UseCaseRoute("CU11", "Enter for registration", RequestMethod.PUT, "/api/v1/testimonies/{id}"),
                new UseCaseRoute("CU12", "Retirar testimony", RequestMethod.POST, "/api/v1/copies"),
                new UseCaseRoute("CU13", "Ver history of management", RequestMethod.GET, "/api/v1/history"),
                new UseCaseRoute("CU14", "Query status management", RequestMethod.GET, "/api/v1/management-statuses"),
                new UseCaseRoute("CU15", "Procesar pago", RequestMethod.POST, "/api/v1/payments"),
                new UseCaseRoute("CU16", "Archivar Management", RequestMethod.PUT, "/api/v1/managements/{id}"),
                new UseCaseRoute("CU17", "Dar Alta person", RequestMethod.POST, "/api/v1/people"),
                new UseCaseRoute("CU18", "Dar Alta cliente", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU19", "Search managements of a Client", RequestMethod.GET, "/api/v1/managements"),
                new UseCaseRoute("CU20", "Create user", RequestMethod.POST, "/api/v1/users"),
                new UseCaseRoute("CU21", "Modificar UserAccount", RequestMethod.PUT, "/api/v1/users/{id}"),
                new UseCaseRoute("CU22", "Registrar Substitution", RequestMethod.POST, "/api/v1/substitutions"),
                new UseCaseRoute("CU23", "View user activity log", RequestMethod.GET, "/api/v1/audit-log/user/{idUser}"),
                new UseCaseRoute("CU24", "Generar libro de indices", RequestMethod.GET, "/api/v1/reports/index-book"),
                new UseCaseRoute("CU25", "Generate DDJJ of the month", RequestMethod.GET, "/api/v1/reports/monthly-sworn-statement"),
                new UseCaseRoute("CU26", "Enter new type of procedure", RequestMethod.POST, "/api/v1/procedure-types"),
                new UseCaseRoute("CU27", "Enter new type of document", RequestMethod.POST, "/api/v1/document-types"),
                new UseCaseRoute("CU28", "Ingresar nuevos folios", RequestMethod.POST, "/api/v1/folios"),
                new UseCaseRoute("CU29", "Enter new concept", RequestMethod.POST, "/api/v1/concepts"),
                new UseCaseRoute("CU30", "Enter new status of Management", RequestMethod.POST, "/api/v1/management-statuses"),
                new UseCaseRoute("CU31", "Update type of procedure", RequestMethod.PUT, "/api/v1/procedure-types/{id}"),
                new UseCaseRoute("CU32", "Modificar type de documento", RequestMethod.PUT, "/api/v1/document-types/{id}"),
                new UseCaseRoute("CU33", "Modificar folio", RequestMethod.PUT, "/api/v1/folios/{id}"),
                new UseCaseRoute("CU34", "Update concept", RequestMethod.PUT, "/api/v1/concepts/{id}"),
                new UseCaseRoute("CU35", "Update status of Management", RequestMethod.PUT, "/api/v1/management-statuses/{id}"),
                new UseCaseRoute("CU36", "Ingresar tipos de folio", RequestMethod.POST, "/api/v1/folio-types"),
                new UseCaseRoute("CU37", "Delete concept", RequestMethod.DELETE, "/api/v1/concepts/{id}"),
                new UseCaseRoute("CU38", "Delete type of document", RequestMethod.DELETE, "/api/v1/document-types/{id}"),
                new UseCaseRoute("CU39", "Create Template Budget", RequestMethod.POST, "/api/v1/budget-templates"),
                new UseCaseRoute("CU40", "Modificar type de folio", RequestMethod.PUT, "/api/v1/folio-types/{id}"),
                new UseCaseRoute("CU41", "Modificar Cliente", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU42", "Informar proximos vencimientos", RequestMethod.GET, "/api/v1/reports/documents-due/{idDocumentoPresentado}"),
                new UseCaseRoute("CU43", "Reenter documentation", RequestMethod.PUT, "/api/v1/submitted-documents/{id}"),
                new UseCaseRoute("CU44", "Reingresar testimony", RequestMethod.PUT, "/api/v1/testimonies/{id}"),
                new UseCaseRoute("CU45", "Modificar budget", RequestMethod.PUT, "/api/v1/budgets/{id}"),
                new UseCaseRoute("CU46", "Ver detalle cliente", RequestMethod.GET, "/api/v1/people/{id}"),
                new UseCaseRoute("CU47", "Consultar Payment", RequestMethod.GET, "/api/v1/payments/budget/{idBudget}"),
                new UseCaseRoute("CU48", "Dar alta notary", RequestMethod.POST, "/api/v1/people"),
                new UseCaseRoute("CU49", "Delete Template Budget", RequestMethod.DELETE, "/api/v1/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}"),
                new UseCaseRoute("CU50", "Generar DDJJ Rentas", RequestMethod.GET, "/api/v1/reports/tax-sworn-statement"),
                new UseCaseRoute("CU51", "Modificar notary", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU52", "Modificar Deed", RequestMethod.PUT, "/api/v1/deeds/{id}"),
                new UseCaseRoute("CU53", "Update Management", RequestMethod.PUT, "/api/v1/managements/{id}"),
                new UseCaseRoute("CU54", "Modificar Person", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU55", "Update Template Budget", RequestMethod.PUT, "/api/v1/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}"),
                new UseCaseRoute("CU56", "Record registration", RequestMethod.PUT, "/api/v1/testimonies/{id}"),
                new UseCaseRoute("CU57", "Delete type of procedure", RequestMethod.DELETE, "/api/v1/procedure-types/{id}"),
                new UseCaseRoute("CU58", "Delete type of folio", RequestMethod.DELETE, "/api/v1/folio-types/{id}"),
                new UseCaseRoute("CU59", "Consultar Suplencias", RequestMethod.GET, "/api/v1/substitutions"),
                new UseCaseRoute("CU60", "Search Budget", RequestMethod.GET, "/api/v1/budgets"),
                new UseCaseRoute("CU61", "Search person or client", RequestMethod.GET, "/api/v1/people/search"),
                new UseCaseRoute("CU62", "Search Deed", RequestMethod.GET, "/api/v1/deeds"),
                new UseCaseRoute("CU63", "Search Folios", RequestMethod.GET, "/api/v1/folios"),
                new UseCaseRoute("CU64", "Search Procedure type", RequestMethod.GET, "/api/v1/procedure-types"),
                new UseCaseRoute("CU65", "Search Tipos of documents", RequestMethod.GET, "/api/v1/document-types"),
                new UseCaseRoute("CU66", "Search Concepts", RequestMethod.GET, "/api/v1/concepts"),
                new UseCaseRoute("CU67", "Search Management statuses", RequestMethod.GET, "/api/v1/management-statuses"),
                new UseCaseRoute("CU68", "Search tipos of folios", RequestMethod.GET, "/api/v1/folio-types"),
                new UseCaseRoute("CU83", "Manage Workflow", RequestMethod.POST, "/api/v1/workflow-definition"),
                new UseCaseRoute("CU83", "Manage Transitions Workflow", RequestMethod.POST, "/api/v1/workflow-transition"),
                new UseCaseRoute("CU83", "Validar Consistencia Workflow", RequestMethod.POST, "/api/v1/workflow-definition/{id}/validate"),
                new UseCaseRoute("CU83", "Asignar Workflow a Tipo de Procedure", RequestMethod.PUT, "/api/v1/procedure-types/{id}/workflow")
        );
    }
}
