package com.licensis.notaire.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Describes a read of personal data for the audit trail. It reads only the request path and
 * numeric id arguments, never string arguments or the query string, because search terms are
 * themselves personal data.
 */
final class PersonalDataReadDescriber {

    private PersonalDataReadDescriber() {
    }

    static String describe(Object[] args) {
        StringBuilder detail = new StringBuilder("Read of personal data");
        Integer id = firstIntegerArgument(args);
        if (id != null) {
            detail.append(" of person with ID ").append(id);
        }
        String path = requestPath();
        if (path != null) {
            detail.append(" via ").append(path);
        }
        return detail.toString();
    }

    private static Integer firstIntegerArgument(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Integer id) {
                return id;
            }
        }
        return null;
    }

    private static String requestPath() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return request.getRequestURI();
        }
        return null;
    }
}
