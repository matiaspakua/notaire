package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.UserRepository;
import com.licensis.notaire.security.UserAuthorityResolver;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthorityResolver — authority from the stored user (issue #559)")
class UserAuthorityResolverTest {

    @Mock
    private UserRepository userRepository;

    private List<String> authoritiesOf(String username) {
        return new UserAuthorityResolver(userRepository).resolve(username)
                .orElseThrow()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Escribano", "ESCRIBANO", "Admin", "administrador", " Administrador "})
    @DisplayName("should grant administrator authority to administrator-capable types")
    void shouldGrantAdministratorAuthorityToAdministratorCapableTypes(String type) {
        when(userRepository.findByName("boss")).thenReturn(Optional.of(new User(1, "boss", "x", true, type)));

        assertThat(authoritiesOf("boss")).containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("should grant user authority only to other types")
    void shouldGrantUserAuthorityOnlyToOtherTypes() {
        when(userRepository.findByName("clerk")).thenReturn(Optional.of(new User(2, "clerk", "x", true, "EMPLEADO")));

        assertThat(authoritiesOf("clerk")).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("should grant user authority when the type is missing")
    void shouldGrantUserAuthorityWhenTypeIsMissing() {
        when(userRepository.findByName("typeless")).thenReturn(Optional.of(new User(3, "typeless", "x", true, null)));

        assertThat(authoritiesOf("typeless")).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("should resolve nothing for an inactive user")
    void shouldResolveNothingForAnInactiveUser() {
        when(userRepository.findByName("gone")).thenReturn(Optional.of(new User(4, "gone", "x", false, "Escribano")));

        assertThat(new UserAuthorityResolver(userRepository).resolve("gone")).isEmpty();
    }

    @Test
    @DisplayName("should resolve nothing for an unknown user")
    void shouldResolveNothingForAnUnknownUser() {
        when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

        assertThat(new UserAuthorityResolver(userRepository).resolve("ghost")).isEmpty();
    }
}
