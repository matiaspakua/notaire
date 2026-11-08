package com.licensis.notaire.unit.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.security.StandardRoleModules;

@DisplayName("StandardRoleModules — V50 template fallback (issue #1683)")
class StandardRoleModulesTest {

    @Test
    @DisplayName("should grant office staff catalog read via administracion module (#559)")
    void shouldGrantOfficeStaffCatalogReadViaAdministracionModule() {
        assertThat(StandardRoleModules.fallbackForUserType("EMPLEADO")).contains("administracion");
    }

    @Test
    @DisplayName("should not grant administracion to notary fallback template")
    void shouldNotGrantAdministracionToNotaryFallbackTemplate() {
        assertThat(StandardRoleModules.fallbackForUserType("ESCRIBANO")).doesNotContain("administracion");
    }
}
