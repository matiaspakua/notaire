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

    public static final String NOTARY_ROLE = "NOTARY";

    private static final Set<String> ADMINISTRATOR_TYPES = Set.of("ADMIN", "ADMINISTRADOR");
    private static final Set<String> NOTARY_TYPES = Set.of("ESCRIBANO");
    private static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_" + ADMIN_ROLE);
    private static final GrantedAuthority NOTARY_AUTHORITY = new SimpleGrantedAuthority("ROLE_" + NOTARY_ROLE);
    private static final GrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");

    private final UserRepository userRepository;

    public UserAuthorityResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<List<GrantedAuthority>> resolve(String username) {
        return userRepository.findByName(username)
                .filter(User::getStatus)
                .map(user -> List.of(authorityOf(user)));
    }

    private GrantedAuthority authorityOf(User user) {
        String type = user.getType() == null ? "" : user.getType().trim().toUpperCase(Locale.ROOT);
        if (ADMINISTRATOR_TYPES.contains(type)) {
            return ADMIN_AUTHORITY;
        }
        return NOTARY_TYPES.contains(type) ? NOTARY_AUTHORITY : USER_AUTHORITY;
    }
}
