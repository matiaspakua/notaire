package com.licensis.notaire.unit.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.licensis.notaire.application.port.in.user.QueryRolesUseCase;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.domain.user.Role;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.security.RoleModuleGrantService;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleModuleGrantService — grants from role_modules (issue #1683)")
class RoleModuleGrantServiceTest {

    @Mock
    private QueryUsersUseCase users;

    @Mock
    private QueryRolesUseCase roles;

    @Test
    @DisplayName("should use modules from the assigned role when present")
    void shouldUseModulesFromTheAssignedRoleWhenPresent() {
        when(users.findByName("clerk")).thenReturn(Optional.of(active("clerk", "EMPLEADO", 7)));
        when(roles.findById(7)).thenReturn(Optional.of(
                Role.restore(7, "Clerk", null, true, List.of("personas", "presupuestos"), 0)));

        Set<String> grants = new RoleModuleGrantService(users, roles).grantedModules("clerk");

        assertThat(grants).containsExactlyInAnyOrder("personas", "presupuestos");
    }

    @Test
    @DisplayName("should fall back to the notary template when a notary has no role")
    void shouldFallBackToTheNotaryTemplateWhenANotaryHasNoRole() {
        when(users.findByName("notary")).thenReturn(Optional.of(active("notary", "ESCRIBANO", null)));

        Set<String> grants = new RoleModuleGrantService(users, roles).grantedModules("notary");

        assertThat(grants).contains("personas", "presupuestos", "escrituras");
        assertThat(grants).doesNotContain("usuarios", "administracion");
    }

    @Test
    @DisplayName("should return empty when the user is unknown")
    void shouldReturnEmptyWhenTheUserIsUnknown() {
        when(users.findByName("ghost")).thenReturn(Optional.empty());

        assertThat(new RoleModuleGrantService(users, roles).grantedModules("ghost")).isEmpty();
    }

    private static User active(String name, String type, Integer roleId) {
        return User.restore(1, name, "x", true, type, null, null, null, roleId, null, 0);
    }
}
