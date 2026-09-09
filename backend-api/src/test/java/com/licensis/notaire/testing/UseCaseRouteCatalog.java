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
                new UseCaseRoute("CU01", "Preparar Presupuesto", RequestMethod.POST, "/api/v1/presupuestos"),
                new UseCaseRoute("CU02", "Iniciar Gestion", RequestMethod.POST, "/api/v1/gestiones"),
                new UseCaseRoute("CU03", "Lista documents y certificados necesarios", RequestMethod.GET, "/api/v1/reportes/lista-documentos-tramite"),
                new UseCaseRoute("CU04", "Registrar documentacion cliente", RequestMethod.POST, "/api/v1/documento-presentado"),
                new UseCaseRoute("CU05", "Preparar deed", RequestMethod.POST, "/api/v1/escrituras"),
                new UseCaseRoute("CU06", "Firmar deed", RequestMethod.PUT, "/api/v1/escrituras/{id}"),
                new UseCaseRoute("CU07", "Generar testimony", RequestMethod.POST, "/api/v1/testimonio"),
                new UseCaseRoute("CU08", "Verificar Testimonio", RequestMethod.GET, "/api/v1/testimonio"),
                new UseCaseRoute("CU09", "Registrar deudas documents de Cliente", RequestMethod.GET, "/api/v1/reportes/consultar-deuda-documentos"),
                new UseCaseRoute("CU10", "Registrar movimientos documentacion entidades", RequestMethod.POST, "/api/v1/movimiento-testimonio"),
                new UseCaseRoute("CU11", "Ingresar para inscripcion", RequestMethod.PUT, "/api/v1/testimonio/{id}"),
                new UseCaseRoute("CU12", "Retirar testimony", RequestMethod.POST, "/api/v1/copia"),
                new UseCaseRoute("CU13", "Ver history de gestion", RequestMethod.GET, "/api/v1/historial"),
                new UseCaseRoute("CU14", "Consultar status gestion", RequestMethod.GET, "/api/v1/estado-gestion"),
                new UseCaseRoute("CU15", "Procesar pago", RequestMethod.POST, "/api/v1/pagos"),
                new UseCaseRoute("CU16", "Archivar Gestion", RequestMethod.PUT, "/api/v1/gestiones/{id}"),
                new UseCaseRoute("CU17", "Dar Alta person", RequestMethod.POST, "/api/v1/people"),
                new UseCaseRoute("CU18", "Dar Alta cliente", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU19", "Buscar gestiones de un Cliente", RequestMethod.GET, "/api/v1/gestiones"),
                new UseCaseRoute("CU20", "Dar alta usuario", RequestMethod.POST, "/api/v1/usuarios"),
                new UseCaseRoute("CU21", "Modificar Usuario", RequestMethod.PUT, "/api/v1/usuarios/{id}"),
                new UseCaseRoute("CU22", "Registrar Suplencia", RequestMethod.POST, "/api/v1/suplencia"),
                new UseCaseRoute("CU23", "View user activity log", RequestMethod.GET, "/api/v1/audit-log/user/{idUser}"),
                new UseCaseRoute("CU24", "Generar libro de indices", RequestMethod.GET, "/api/v1/reportes/libro-indice"),
                new UseCaseRoute("CU25", "Generar DDJJ del mes", RequestMethod.GET, "/api/v1/reportes/declaracion-jurada-mensual"),
                new UseCaseRoute("CU26", "Ingresar nuevo type de tramite", RequestMethod.POST, "/api/v1/tipo-tramite"),
                new UseCaseRoute("CU27", "Ingresar nuevo type de documento", RequestMethod.POST, "/api/v1/tipo-de-documento"),
                new UseCaseRoute("CU28", "Ingresar nuevos folios", RequestMethod.POST, "/api/v1/folio"),
                new UseCaseRoute("CU29", "Ingresar nuevo concepto", RequestMethod.POST, "/api/v1/conceptos"),
                new UseCaseRoute("CU30", "Ingresar nuevo status de Gestion", RequestMethod.POST, "/api/v1/estado-gestion"),
                new UseCaseRoute("CU31", "Modificar type de tramite", RequestMethod.PUT, "/api/v1/tipo-tramite/{id}"),
                new UseCaseRoute("CU32", "Modificar type de documento", RequestMethod.PUT, "/api/v1/tipo-de-documento/{id}"),
                new UseCaseRoute("CU33", "Modificar folio", RequestMethod.PUT, "/api/v1/folio/{id}"),
                new UseCaseRoute("CU34", "Modificar concepto", RequestMethod.PUT, "/api/v1/conceptos/{id}"),
                new UseCaseRoute("CU35", "Modificar status de Gestion", RequestMethod.PUT, "/api/v1/estado-gestion/{id}"),
                new UseCaseRoute("CU36", "Ingresar tipos de folio", RequestMethod.POST, "/api/v1/tipo-folio"),
                new UseCaseRoute("CU37", "Eliminar concepto", RequestMethod.DELETE, "/api/v1/conceptos/{id}"),
                new UseCaseRoute("CU38", "Eliminar type de documento", RequestMethod.DELETE, "/api/v1/tipo-de-documento/{id}"),
                new UseCaseRoute("CU39", "Crear Plantilla Presupuesto", RequestMethod.POST, "/api/v1/plantilla-presupuestos"),
                new UseCaseRoute("CU40", "Modificar type de folio", RequestMethod.PUT, "/api/v1/tipo-folio/{id}"),
                new UseCaseRoute("CU41", "Modificar Cliente", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU42", "Informar proximos vencimientos", RequestMethod.GET, "/api/v1/reportes/documentos-por-vencer/{idDocumentoPresentado}"),
                new UseCaseRoute("CU43", "Reingresar documentacion", RequestMethod.PUT, "/api/v1/documento-presentado/{id}"),
                new UseCaseRoute("CU44", "Reingresar testimony", RequestMethod.PUT, "/api/v1/testimonio/{id}"),
                new UseCaseRoute("CU45", "Modificar budget", RequestMethod.PUT, "/api/v1/presupuestos/{id}"),
                new UseCaseRoute("CU46", "Ver detalle cliente", RequestMethod.GET, "/api/v1/people/{id}"),
                new UseCaseRoute("CU47", "Consultar Pago", RequestMethod.GET, "/api/v1/pagos/presupuesto/{idBudget}"),
                new UseCaseRoute("CU48", "Dar alta notary", RequestMethod.POST, "/api/v1/people"),
                new UseCaseRoute("CU49", "Eliminar Plantilla Presupuesto", RequestMethod.DELETE, "/api/v1/plantilla-presupuestos/tipo-tramite/{idProcedureType}/concepto/{idConcept}"),
                new UseCaseRoute("CU50", "Generar DDJJ Rentas", RequestMethod.GET, "/api/v1/reportes/declaracion-jurada-rentas"),
                new UseCaseRoute("CU51", "Modificar notary", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU52", "Modificar Escritura", RequestMethod.PUT, "/api/v1/escrituras/{id}"),
                new UseCaseRoute("CU53", "Modificar Gestion", RequestMethod.PUT, "/api/v1/gestiones/{id}"),
                new UseCaseRoute("CU54", "Modificar Persona", RequestMethod.PUT, "/api/v1/people/{id}"),
                new UseCaseRoute("CU55", "Modificar Plantilla Presupuesto", RequestMethod.PUT, "/api/v1/plantilla-presupuestos/tipo-tramite/{idProcedureType}/concepto/{idConcept}"),
                new UseCaseRoute("CU56", "Registrar inscripcion", RequestMethod.PUT, "/api/v1/testimonio/{id}"),
                new UseCaseRoute("CU57", "Eliminar type de tramite", RequestMethod.DELETE, "/api/v1/tipo-tramite/{id}"),
                new UseCaseRoute("CU58", "Eliminar type de folio", RequestMethod.DELETE, "/api/v1/tipo-folio/{id}"),
                new UseCaseRoute("CU59", "Consultar Suplencias", RequestMethod.GET, "/api/v1/suplencia"),
                new UseCaseRoute("CU60", "Buscar Presupuesto", RequestMethod.GET, "/api/v1/presupuestos"),
                new UseCaseRoute("CU61", "Buscar person o cliente", RequestMethod.GET, "/api/v1/people/search"),
                new UseCaseRoute("CU62", "Buscar Escritura", RequestMethod.GET, "/api/v1/escrituras"),
                new UseCaseRoute("CU63", "Buscar Folios", RequestMethod.GET, "/api/v1/folio"),
                new UseCaseRoute("CU64", "Buscar Tipo de tramite", RequestMethod.GET, "/api/v1/tipo-tramite"),
                new UseCaseRoute("CU65", "Buscar Tipos de documents", RequestMethod.GET, "/api/v1/tipo-de-documento"),
                new UseCaseRoute("CU66", "Buscar Conceptos", RequestMethod.GET, "/api/v1/conceptos"),
                new UseCaseRoute("CU67", "Buscar Estados de Gestion", RequestMethod.GET, "/api/v1/estado-gestion"),
                new UseCaseRoute("CU68", "Buscar tipos de folios", RequestMethod.GET, "/api/v1/tipo-folio"),
                new UseCaseRoute("CU83", "Gestionar Workflow", RequestMethod.POST, "/api/v1/workflow-definition"),
                new UseCaseRoute("CU83", "Gestionar Transiciones Workflow", RequestMethod.POST, "/api/v1/workflow-transition"),
                new UseCaseRoute("CU83", "Validar Consistencia Workflow", RequestMethod.POST, "/api/v1/workflow-definition/{id}/validate"),
                new UseCaseRoute("CU83", "Asignar Workflow a Tipo de Tramite", RequestMethod.PUT, "/api/v1/tipo-tramite/{id}/workflow")
        );
    }
}
