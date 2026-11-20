package com.licensis.notaire.security;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.domain.user.AccountRole;
import com.licensis.notaire.domain.user.User;

/**
 * Derives the request authority from the stored user, so a type change or a
 * deactivation applies immediately instead of when the token expires (issue #559).
 */
@Component
public class UserAuthorityResolver {

    public static final String ADMIN_ROLE = "ADMIN";

    public static final String NOTARY_ROLE = "NOTARY";

    private static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_" + ADMIN_ROLE);
    private static final GrantedAuthority NOTARY_AUTHORITY = new SimpleGrantedAuthority("ROLE_" + NOTARY_ROLE);
    private static final GrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");

    private final QueryUsersUseCase users;

    public UserAuthorityResolver(QueryUsersUseCase users) {
        this.users = users;
    }

    public Optional<List<GrantedAuthority>> resolve(String username) {
        return users.findByName(username)
                .filter(User::active)
                .map(user -> List.of(authorityOf(user)));
    }

    private GrantedAuthority authorityOf(User user) {
        return switch (AccountRole.ofUserType(user.type())) {
            case ADMIN -> ADMIN_AUTHORITY;
            case NOTARY -> NOTARY_AUTHORITY;
            case USER -> USER_AUTHORITY;
        };
    }
}
