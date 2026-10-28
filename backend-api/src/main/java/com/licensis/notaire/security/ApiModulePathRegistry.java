package com.licensis.notaire.security;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Maps API paths to the {@code role_modules.module} keys edited in role administration
 * (issue #1683 / ADR-029 W2). Unmapped paths are not module-gated.
 */
public final class ApiModulePathRegistry {

    private record PrefixRule(String prefix, String module) { }

    private static final List<PrefixRule> RULES = buildRules();

    private ApiModulePathRegistry() {
    }

    public static Optional<String> requiredModule(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return Optional.empty();
        }
        String path = requestUri;
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        if ("/api/v1/users/login".equals(path) || "/api/v1/users/logout".equals(path)) {
            return Optional.empty();
        }
        for (PrefixRule rule : RULES) {
            if (path.equals(rule.prefix()) || path.startsWith(rule.prefix() + "/")) {
                return Optional.of(rule.module());
            }
        }
        return Optional.empty();
    }

    private static List<PrefixRule> buildRules() {
        List<PrefixRule> rules = new ArrayList<>();
        add(rules, "/api/v1/users", "usuarios");
        add(rules, "/api/v1/roles", "administracion");
        add(rules, "/api/v1/audit-log", "auditoria");
        add(rules, "/api/v1/people", "personas");
        add(rules, "/api/v1/properties", "personas");
        add(rules, "/api/v1/payments", "presupuestos");
        add(rules, "/api/v1/budgets", "presupuestos");
        add(rules, "/api/v1/budget-templates", "administracion");
        add(rules, "/api/v1/managements", "tramites");
        add(rules, "/api/v1/procedures", "tramites");
        add(rules, "/api/v1/procedure-folders", "tramites");
        add(rules, "/api/v1/submitted-documents", "tramites");
        add(rules, "/api/v1/history", "tramites");
        add(rules, "/api/v1/items", "tramites");
        add(rules, "/api/v1/reports", "tramites");
        add(rules, "/api/v1/substitutions", "personas");
        add(rules, "/api/v1/deeds", "escrituras");
        add(rules, "/api/v1/testimonies", "escrituras");
        add(rules, "/api/v1/testimony-movements", "escrituras");
        add(rules, "/api/v1/copies", "escrituras");
        add(rules, "/api/v1/registration-drafts", "escrituras");
        add(rules, "/api/v1/folios", "escrituras");
        add(rules, "/api/v1/notebooks", "escrituras");
        add(rules, "/api/v1/auxiliary-protocol", "escrituras");
        add(rules, "/api/v1/workflow-definition", "workflows");
        add(rules, "/api/v1/workflow-node", "workflows");
        add(rules, "/api/v1/workflow-transition", "workflows");
        add(rules, "/api/v1/concepts", "administracion");
        add(rules, "/api/v1/document-types", "administracion");
        add(rules, "/api/v1/folio-types", "administracion");
        add(rules, "/api/v1/procedure-types", "administracion");
        add(rules, "/api/v1/identification-types", "administracion");
        add(rules, "/api/v1/management-statuses", "administracion");
        add(rules, "/api/v1/procedure-templates", "administracion");
        add(rules, "/api/v1/document-cost-templates", "administracion");
        rules.sort(Comparator.comparingInt((PrefixRule rule) -> rule.prefix().length()).reversed());
        return List.copyOf(rules);
    }

    private static void add(List<PrefixRule> rules, String prefix, String module) {
        rules.add(new PrefixRule(prefix, module));
    }
}
