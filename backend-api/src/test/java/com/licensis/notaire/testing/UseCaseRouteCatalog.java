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
                new UseCaseRoute("CU03", "Lista documentos y certificados necesarios", RequestMethod.GET, "/api/v1/reportes/lista-documentos-tramite"),
                new UseCaseRoute("CU04", "Registrar documentacion cliente", RequestMethod.POST, "/api/v1/documento-presentado"),
                new UseCaseRoute("CU05", "Preparar escritura", RequestMethod.POST, "/api/v1/escrituras"),
                new UseCaseRoute("CU06", "Firmar escritura", RequestMethod.PUT, "/api/v1/escrituras/{id}"),
                new UseCaseRoute("CU07", "Generar testimonio", RequestMethod.POST, "/api/v1/testimonio"),
                new UseCaseRoute("CU08", "Verificar Testimonio", RequestMethod.GET, "/api/v1/testimonio"),
                new UseCaseRoute("CU09", "Registrar deudas documentos de Cliente", RequestMethod.GET, "/api/v1/reportes/consultar-deuda-documentos"),
                new UseCaseRoute("CU10", "Registrar movimientos documentacion entidades", RequestMethod.POST, "/api/v1/movimiento-testimonio"),
                new UseCaseRoute("CU11", "Ingresar para inscripcion", RequestMethod.PUT, "/api/v1/testimonio/{id}"),
                new UseCaseRoute("CU12", "Retirar testimonio", RequestMethod.POST, "/api/v1/copia"),
                new UseCaseRoute("CU13", "Ver historial de gestion", RequestMethod.GET, "/api/v1/historial"),
                new UseCaseRoute("CU14", "Consultar estado gestion", RequestMethod.GET, "/api/v1/estado-gestion"),
                new UseCaseRoute("CU15", "Procesar pago", RequestMethod.POST, "/api/v1/pagos"),
                new UseCaseRoute("CU16", "Archivar Gestion", RequestMethod.PUT, "/api/v1/gestiones/{id}"),
                new UseCaseRoute("CU17", "Dar Alta persona", RequestMethod.POST, "/api/v1/personas"),
                new UseCaseRoute("CU18", "Dar Alta cliente", RequestMethod.PUT, "/api/v1/personas/{id}"),
                new UseCaseRoute("CU19", "Buscar gestiones de un Cliente", RequestMethod.GET, "/api/v1/gestiones"),
                new UseCaseRoute("CU20", "Dar alta usuario", RequestMethod.POST, "/api/v1/usuarios"),
                new UseCaseRoute("CU21", "Modificar Usuario", RequestMethod.PUT, "/api/v1/usuarios/{id}"),
                new UseCaseRoute("CU22", "Registrar Suplencia", RequestMethod.POST, "/api/v1/suplencia"),
                new UseCaseRoute("CU23", "Ver registro actividades usuario", RequestMethod.GET, "/api/v1/registro-auditoria/usuario/{idUsuario}"),
                new UseCaseRoute("CU24", "Generar libro de indices", RequestMethod.GET, "/api/v1/reportes/libro-indice"),
                new UseCaseRoute("CU25", "Generar DDJJ del mes", RequestMethod.GET, "/api/v1/reportes/declaracion-jurada-mensual"),
                new UseCaseRoute("CU26", "Ingresar nuevo tipo de tramite", RequestMethod.POST, "/api/v1/tipo-tramite"),
                new UseCaseRoute("CU27", "Ingresar nuevo tipo de documento", RequestMethod.POST, "/api/v1/tipo-de-documento"),
                new UseCaseRoute("CU28", "Ingresar nuevos folios", RequestMethod.POST, "/api/v1/folio"),
                new UseCaseRoute("CU29", "Ingresar nuevo concepto", RequestMethod.POST, "/api/v1/conceptos"),
                new UseCaseRoute("CU30", "Ingresar nuevo estado de Gestion", RequestMethod.POST, "/api/v1/estado-gestion"),
                new UseCaseRoute("CU31", "Modificar tipo de tramite", RequestMethod.PUT, "/api/v1/tipo-tramite/{id}"),
                new UseCaseRoute("CU32", "Modificar tipo de documento", RequestMethod.PUT, "/api/v1/tipo-de-documento/{id}"),
                new UseCaseRoute("CU33", "Modificar folio", RequestMethod.PUT, "/api/v1/folio/{id}"),
                new UseCaseRoute("CU34", "Modificar concepto", RequestMethod.PUT, "/api/v1/conceptos/{id}"),
                new UseCaseRoute("CU35", "Modificar estado de Gestion", RequestMethod.PUT, "/api/v1/estado-gestion/{id}"),
                new UseCaseRoute("CU36", "Ingresar tipos de folio", RequestMethod.POST, "/api/v1/tipo-folio"),
                new UseCaseRoute("CU37", "Eliminar concepto", RequestMethod.DELETE, "/api/v1/conceptos/{id}"),
                new UseCaseRoute("CU38", "Eliminar tipo de documento", RequestMethod.DELETE, "/api/v1/tipo-de-documento/{id}"),
                new UseCaseRoute("CU39", "Crear Plantilla Presupuesto", RequestMethod.POST, "/api/v1/plantilla-presupuestos"),
                new UseCaseRoute("CU40", "Modificar tipo de folio", RequestMethod.PUT, "/api/v1/tipo-folio/{id}"),
                new UseCaseRoute("CU41", "Modificar Cliente", RequestMethod.PUT, "/api/v1/personas/{id}"),
                new UseCaseRoute("CU42", "Informar proximos vencimientos", RequestMethod.GET, "/api/v1/reportes/documentos-por-vencer/{idDocumentoPresentado}"),
                new UseCaseRoute("CU43", "Reingresar documentacion", RequestMethod.PUT, "/api/v1/documento-presentado/{id}"),
                new UseCaseRoute("CU44", "Reingresar testimonio", RequestMethod.PUT, "/api/v1/testimonio/{id}"),
                new UseCaseRoute("CU45", "Modificar presupuesto", RequestMethod.PUT, "/api/v1/presupuestos/{id}"),
                new UseCaseRoute("CU46", "Ver detalle cliente", RequestMethod.GET, "/api/v1/personas/{id}"),
                new UseCaseRoute("CU47", "Consultar Pago", RequestMethod.GET, "/api/v1/pagos/presupuesto/{idPresupuesto}"),
                new UseCaseRoute("CU48", "Dar alta escribano", RequestMethod.POST, "/api/v1/personas"),
                new UseCaseRoute("CU49", "Eliminar Plantilla Presupuesto", RequestMethod.DELETE, "/api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}"),
                new UseCaseRoute("CU50", "Generar DDJJ Rentas", RequestMethod.GET, "/api/v1/reportes/declaracion-jurada-rentas"),
                new UseCaseRoute("CU51", "Modificar escribano", RequestMethod.PUT, "/api/v1/personas/{id}"),
                new UseCaseRoute("CU52", "Modificar Escritura", RequestMethod.PUT, "/api/v1/escrituras/{id}"),
                new UseCaseRoute("CU53", "Modificar Gestion", RequestMethod.PUT, "/api/v1/gestiones/{id}"),
                new UseCaseRoute("CU54", "Modificar Persona", RequestMethod.PUT, "/api/v1/personas/{id}"),
                new UseCaseRoute("CU55", "Modificar Plantilla Presupuesto", RequestMethod.PUT, "/api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}"),
                new UseCaseRoute("CU56", "Registrar inscripcion", RequestMethod.PUT, "/api/v1/testimonio/{id}"),
                new UseCaseRoute("CU57", "Eliminar tipo de tramite", RequestMethod.DELETE, "/api/v1/tipo-tramite/{id}"),
                new UseCaseRoute("CU58", "Eliminar tipo de folio", RequestMethod.DELETE, "/api/v1/tipo-folio/{id}"),
                new UseCaseRoute("CU59", "Consultar Suplencias", RequestMethod.GET, "/api/v1/suplencia"),
                new UseCaseRoute("CU60", "Buscar Presupuesto", RequestMethod.GET, "/api/v1/presupuestos"),
                new UseCaseRoute("CU61", "Buscar persona o cliente", RequestMethod.GET, "/api/v1/personas/buscar"),
                new UseCaseRoute("CU62", "Buscar Escritura", RequestMethod.GET, "/api/v1/escrituras"),
                new UseCaseRoute("CU63", "Buscar Folios", RequestMethod.GET, "/api/v1/folio"),
                new UseCaseRoute("CU64", "Buscar Tipo de tramite", RequestMethod.GET, "/api/v1/tipo-tramite"),
                new UseCaseRoute("CU65", "Buscar Tipos de documentos", RequestMethod.GET, "/api/v1/tipo-de-documento"),
                new UseCaseRoute("CU66", "Buscar Conceptos", RequestMethod.GET, "/api/v1/conceptos"),
                new UseCaseRoute("CU67", "Buscar Estados de Gestion", RequestMethod.GET, "/api/v1/estado-gestion"),
                new UseCaseRoute("CU68", "Buscar tipos de folios", RequestMethod.GET, "/api/v1/tipo-folio")
        );
    }
}
