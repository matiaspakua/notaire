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

import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.security.UserAuthorityResolver;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthorityResolver — authority from the stored user (issue #559)")
class UserAuthorityResolverTest {

    @Mock
    private QueryUsersUseCase users;

    private List<String> authoritiesOf(String username) {
        return new UserAuthorityResolver(users).resolve(username)
                .orElseThrow()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Admin", "ADMIN", "administrador", " Administrador "})
    @DisplayName("should grant administrator authority to administrator types")
    void shouldGrantAdministratorAuthorityToAdministratorCapableTypes(String type) {
        when(users.findByName("boss")).thenReturn(Optional.of(user("boss", true, type)));

        assertThat(authoritiesOf("boss")).containsExactly("ROLE_ADMIN");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Escribano", "ESCRIBANO", " escribano "})
    @DisplayName("should grant the notary authority, not the administrator one, to notaries (issue #1638)")
    void shouldGrantTheNotaryAuthorityToNotaries(String type) {
        when(users.findByName("notary")).thenReturn(Optional.of(user("notary", true, type)));

        assertThat(authoritiesOf("notary")).containsExactly("ROLE_NOTARY");
    }

    @Test
    @DisplayName("should grant user authority only to other types")
    void shouldGrantUserAuthorityOnlyToOtherTypes() {
        when(users.findByName("clerk")).thenReturn(Optional.of(user("clerk", true, "EMPLEADO")));

        assertThat(authoritiesOf("clerk")).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("should grant user authority when the type is missing")
    void shouldGrantUserAuthorityWhenTypeIsMissing() {
        when(users.findByName("typeless")).thenReturn(Optional.of(user("typeless", true, null)));

        assertThat(authoritiesOf("typeless")).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("should resolve nothing for an inactive user")
    void shouldResolveNothingForAnInactiveUser() {
        when(users.findByName("gone")).thenReturn(Optional.of(user("gone", false, "Escribano")));

        assertThat(new UserAuthorityResolver(users).resolve("gone")).isEmpty();
    }

    @Test
    @DisplayName("should resolve nothing for an unknown user")
    void shouldResolveNothingForAnUnknownUser() {
        when(users.findByName("ghost")).thenReturn(Optional.empty());

        assertThat(new UserAuthorityResolver(users).resolve("ghost")).isEmpty();
    }

    private static User user(String name, boolean active, String type) {
        return User.restore(1, name, "x", active, type, null, null, null, null, null, 0);
    }
}
