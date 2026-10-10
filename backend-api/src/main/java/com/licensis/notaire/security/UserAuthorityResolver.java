package com.licensis.notaire.security;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.UserRepository;

/**
 * Derives the request authority from the stored user, so a type change or a
 * deactivation applies immediately instead of when the token expires (issue #559).
 */
@Component
public class UserAuthorityResolver {

    public static final String ADMIN_ROLE = "ADMIN";

    private static final Set<String> ADMINISTRATOR_TYPES = Set.of("ADMIN", "ADMINISTRADOR", "ESCRIBANO");
    private static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_" + ADMIN_ROLE);
    private static final GrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");

    private final UserRepository userRepository;

    public UserAuthorityResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<List<GrantedAuthority>> resolve(String username) {
        return userRepository.findByName(username)
                .filter(User::getStatus)
                .map(user -> List.of(isAdministrator(user) ? ADMIN_AUTHORITY : USER_AUTHORITY));
    }

    private boolean isAdministrator(User user) {
        return user.getType() != null
                && ADMINISTRATOR_TYPES.contains(user.getType().trim().toUpperCase(Locale.ROOT));
    }
}
