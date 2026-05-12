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

    private static final Map<String, String> CONTROLLER_TO_MODULE = Map.ofEntries(
            Map.entry("EscrituraController", "Escrituras"),
            Map.entry("PersonaController", "Personas"),
            Map.entry("PresupuestoController", "Presupuestos"),
            Map.entry("ConceptoController", "Conceptos"),
            Map.entry("UsuarioController", "Usuarios"),
            Map.entry("PagoController", "Pagos"),
            Map.entry("TramiteController", "Tramites"),
            Map.entry("CopiaController", "Copias"),
            Map.entry("TestimonioController", "Testimonios"),
            Map.entry("FolioController", "Folios"),
            Map.entry("ItemController", "Items"),
            Map.entry("InmuebleController", "Inmuebles"),
            Map.entry("HistorialController", "Historial"),
            Map.entry("DocumentoPresentadoController", "Documentos"),
            Map.entry("GestionController", "Gestiones"),
            Map.entry("EstadoDeGestionController", "EstadosDeGestion"),
            Map.entry("MovimientoTestimonioController", "MovimientosTestimonio"),
            Map.entry("SuplenciaController", "Suplencias"),
            Map.entry("PlantillaPresupuestoController", "PlantillasPresupuesto"),
            Map.entry("PlantillaTramiteController", "PlantillasTramite"),
            Map.entry("TipoDeDocumentoController", "TiposDeDocumento"),
            Map.entry("TipoDeFolioController", "TiposDeFolio"),
            Map.entry("TipoDeTramiteController", "TiposDeTramite"),
            Map.entry("TipoIdentificacionController", "TiposIdentificacion"),
            Map.entry("ReporteController", "Reportes")
    );

    private static final String CONTROLLER_SUFFIX = "Controller";
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
        String mapped = CONTROLLER_TO_MODULE.get(controllerSimpleName);
        if (mapped != null) {
            return mapped;
        }
        if (controllerSimpleName.endsWith(CONTROLLER_SUFFIX)) {
            return controllerSimpleName.substring(0,
                    controllerSimpleName.length() - CONTROLLER_SUFFIX.length());
        }
        return controllerSimpleName;
    }
}
