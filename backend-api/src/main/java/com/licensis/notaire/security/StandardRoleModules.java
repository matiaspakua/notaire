package com.licensis.notaire.security;

import java.util.Set;

import com.licensis.notaire.domain.user.AccountRole;

/**
 * Standard role template module grants (ADR-029 / V50). Used when a user account
 * has no {@code fk_id_role} so existing installations keep working until every user
 * is assigned a role.
 */
public final class StandardRoleModules {

    public static final String ADMINISTRATOR_ROLE_NAME = "Administrator";

    public static final String ROLE_MANAGEMENT_MODULE = "administracion";

    private static final Set<String> ADMINISTRATOR_MODULES = Set.of(
            "administracion", "usuarios", "tramites", "presupuestos", "escrituras", "personas", "auditoria",
            "workflows");

    private static final Set<String> NOTARY_MODULES = Set.of(
            "tramites", "presupuestos", "escrituras", "personas", "auditoria", "workflows");

    /** Includes {@code administracion} so catalog GET paths stay reachable (#559 / #1683). Writes remain ADMIN-only in Spring Security. */
    private static final Set<String> OFFICE_STAFF_MODULES = Set.of(
            "tramites", "presupuestos", "escrituras", "personas", "administracion");

    private StandardRoleModules() {
    }

    public static Set<String> fallbackForUserType(String userType) {
        return switch (AccountRole.ofUserType(userType)) {
            case ADMIN -> ADMINISTRATOR_MODULES;
            case NOTARY -> NOTARY_MODULES;
            case USER -> OFFICE_STAFF_MODULES;
        };
    }
}
