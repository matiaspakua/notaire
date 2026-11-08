package com.licensis.notaire.unit.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.licensis.notaire.security.ApiModulePathRegistry;

@DisplayName("ApiModulePathRegistry — API path to role module (issue #1683)")
class ApiModulePathRegistryTest {

    @ParameterizedTest
    @CsvSource({
        "/api/v1/payments,presupuestos",
        "/api/v1/payments/1,presupuestos",
        "/api/v1/payments/budget/10/balance,presupuestos",
        "/api/v1/budgets,presupuestos",
        "/api/v1/people,personas",
        "/api/v1/people/42,personas",
        "/api/v1/deeds,escrituras",
        "/api/v1/managements,tramites",
        "/api/v1/workflow-definition,workflows",
        "/api/v1/users,usuarios",
        "/api/v1/roles,administracion",
        "/api/v1/audit-log,auditoria",
        "/api/v1/concepts,administracion"
    })
    @DisplayName("should map protected API paths to role module keys")
    void shouldMapProtectedApiPathsToRoleModuleKeys(String path, String module) {
        assertThat(ApiModulePathRegistry.requiredModule(path)).contains(module);
    }

    @Test
    @DisplayName("should ignore login and logout paths")
    void shouldIgnoreLoginAndLogoutPaths() {
        assertThat(ApiModulePathRegistry.requiredModule("/api/v1/users/login")).isEmpty();
        assertThat(ApiModulePathRegistry.requiredModule("/api/v1/users/logout")).isEmpty();
    }

    @Test
    @DisplayName("should leave unmapped paths without a module requirement")
    void shouldLeaveUnmappedPathsWithoutAModuleRequirement() {
        assertThat(ApiModulePathRegistry.requiredModule("/api/v1/health")).isEmpty();
    }
}
