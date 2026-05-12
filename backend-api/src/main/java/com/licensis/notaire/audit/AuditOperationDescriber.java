package com.licensis.notaire.audit;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Builds the human-readable Spanish description stored in
 * {@code registro_auditoria.detalle_operacion} from controller method
 * metadata.
 *
 * <p>The descriptions are intentionally written in plain, non-technical
 * Spanish so end users can read the audit trail without needing any
 * implementation knowledge.
 */
public final class AuditOperationDescriber {

    private static final String GENERIC_OBJECT = "registro";

    private AuditOperationDescriber() {
        // Utility class.
    }

    /**
     * Builds the operation description.
     *
     * @param method   the invoked controller method
     * @param args     the runtime arguments passed to the method
     * @param module   the resolved business module name
     * @return a Spanish sentence describing the user action
     */
    public static String describe(Method method, Object[] args, String module) {
        String resourceSingular = toSingularLower(module);
        String resourcePlural;
        if (module == null || module.isBlank()) {
            resourcePlural = GENERIC_OBJECT;
        } else {
            resourcePlural = module.toLowerCase(Locale.ROOT);
        }
        String methodName = method == null ? "" : method.getName().toLowerCase(Locale.ROOT);

        if (methodName.contains("login")) {
            return "Inicio de sesión del usuario";
        }
        if (methodName.contains("logout")) {
            return "Cierre de sesión del usuario";
        }

        HttpVerb verb = detectHttpVerb(method);
        String idText = findIdArgument(args);

        switch (verb) {
            case POST:
                return "Creación de nuevo " + resourceSingular;
            case PUT:
            case PATCH:
                return idText != null
                        ? "Actualización de " + resourceSingular + " con ID " + idText
                        : "Actualización de " + resourceSingular;
            case DELETE:
                return idText != null
                        ? "Eliminación de " + resourceSingular + " con ID " + idText
                        : "Eliminación de " + resourceSingular;
            case GET:
            default:
                if (idText != null) {
                    return "Consulta de " + resourceSingular + " con ID " + idText;
                }
                return "Consulta de listado de " + resourcePlural;
        }
    }

    private static HttpVerb detectHttpVerb(Method method) {
        if (method == null) {
            return HttpVerb.GET;
        }
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof GetMapping) {
                return HttpVerb.GET;
            }
            if (annotation instanceof PostMapping) {
                return HttpVerb.POST;
            }
            if (annotation instanceof PutMapping) {
                return HttpVerb.PUT;
            }
            if (annotation instanceof PatchMapping) {
                return HttpVerb.PATCH;
            }
            if (annotation instanceof DeleteMapping) {
                return HttpVerb.DELETE;
            }
        }
        return HttpVerb.GET;
    }

    private static String findIdArgument(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Integer || arg instanceof Long || arg instanceof String) {
                String value = String.valueOf(arg);
                if (!value.isBlank() && !value.equals("null")) {
                    return value;
                }
            }
        }
        return null;
    }

    private static String toSingularLower(String module) {
        if (module == null || module.isBlank()) {
            return GENERIC_OBJECT;
        }
        String lower = module.toLowerCase(Locale.ROOT);
        if (lower.endsWith("nes")) {
            // gestiones -> gestion
            return lower.substring(0, lower.length() - 2);
        }
        if (lower.endsWith("es")) {
            // tramites -> tramite, copies -> copy (we only deal with Spanish here)
            return lower.substring(0, lower.length() - 1);
        }
        if (lower.endsWith("s")) {
            // escrituras -> escritura, personas -> persona
            return lower.substring(0, lower.length() - 1);
        }
        return lower;
    }

    private enum HttpVerb {
        GET, POST, PUT, PATCH, DELETE
    }
}
