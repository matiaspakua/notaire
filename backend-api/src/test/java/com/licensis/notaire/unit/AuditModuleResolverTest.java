package com.licensis.notaire.unit;

import com.licensis.notaire.audit.AuditModuleResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditModuleResolver unit tests")
class AuditModuleResolverTest {

    @Test
    @DisplayName("Should map EscrituraController to Escrituras")
    void shouldMapEscrituraControllerToEscrituras() {
        assertThat(AuditModuleResolver.resolve("EscrituraController")).isEqualTo("Escrituras");
    }

    @Test
    @DisplayName("Should map PersonaController to Personas")
    void shouldMapPersonaControllerToPersonas() {
        assertThat(AuditModuleResolver.resolve("PersonaController")).isEqualTo("Personas");
    }

    @Test
    @DisplayName("Should map UsuarioController to Usuarios")
    void shouldMapUsuarioControllerToUsuarios() {
        assertThat(AuditModuleResolver.resolve("UsuarioController")).isEqualTo("Usuarios");
    }

    @Test
    @DisplayName("Should strip Controller suffix when not in known map")
    void shouldStripControllerSuffixForUnknownController() {
        assertThat(AuditModuleResolver.resolve("UnknownThingController")).isEqualTo("UnknownThing");
    }

    @Test
    @DisplayName("Should return same name when not ending with Controller")
    void shouldReturnSameNameWhenNoControllerSuffix() {
        assertThat(AuditModuleResolver.resolve("SomeService")).isEqualTo("SomeService");
    }

    @Test
    @DisplayName("Should return General when class name is null")
    void shouldReturnGeneralWhenClassNameNull() {
        assertThat(AuditModuleResolver.resolve(null)).isEqualTo("General");
    }

    @Test
    @DisplayName("Should return General when class name is blank")
    void shouldReturnGeneralWhenClassNameBlank() {
        assertThat(AuditModuleResolver.resolve("   ")).isEqualTo("General");
    }
}
