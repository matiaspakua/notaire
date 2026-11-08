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

import com.licensis.notaire.business.Role;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.UserRepository;
import com.licensis.notaire.security.RoleModuleGrantService;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleModuleGrantService — grants from role_modules (issue #1683)")
class RoleModuleGrantServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("should use modules from the assigned role when present")
    void shouldUseModulesFromTheAssignedRoleWhenPresent() {
        Role role = new Role();
        role.setActive(true);
        role.setModulos(List.of("personas", "presupuestos"));
        User user = new User(1, "clerk", "x", true, "EMPLEADO");
        user.setRole(role);
        when(userRepository.findByName("clerk")).thenReturn(Optional.of(user));

        Set<String> grants = new RoleModuleGrantService(userRepository).grantedModules("clerk");

        assertThat(grants).containsExactlyInAnyOrder("personas", "presupuestos");
    }

    @Test
    @DisplayName("should fall back to the notary template when a notary has no role")
    void shouldFallBackToTheNotaryTemplateWhenANotaryHasNoRole() {
        when(userRepository.findByName("notary")).thenReturn(
                Optional.of(new User(2, "notary", "x", true, "ESCRIBANO")));

        Set<String> grants = new RoleModuleGrantService(userRepository).grantedModules("notary");

        assertThat(grants).contains("personas", "presupuestos", "escrituras");
        assertThat(grants).doesNotContain("usuarios", "administracion");
    }

    @Test
    @DisplayName("should return empty when the user is unknown")
    void shouldReturnEmptyWhenTheUserIsUnknown() {
        when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

        assertThat(new RoleModuleGrantService(userRepository).grantedModules("ghost")).isEmpty();
    }
}
