package com.licensis.notaire.unit;

import com.licensis.notaire.audit.AuditModuleResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditModuleResolver unit tests")
class AuditModuleResolverTest {

    @Test
    @DisplayName("Should map DeedController to Deeds")
    void shouldMapDeedControllerToDeeds() {
        assertThat(AuditModuleResolver.resolve("DeedController")).isEqualTo("Deeds");
    }

    @Test
    @DisplayName("Should map PersonController to People")
    void shouldMapPersonControllerToPeople() {
        assertThat(AuditModuleResolver.resolve("PersonController")).isEqualTo("People");
    }

    @Test
    @DisplayName("Should map UserController to Users")
    void shouldMapUserControllerToUsers() {
        assertThat(AuditModuleResolver.resolve("UserController")).isEqualTo("Users");
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
