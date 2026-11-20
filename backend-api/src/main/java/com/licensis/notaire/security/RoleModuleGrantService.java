package com.licensis.notaire.security;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.in.user.QueryRolesUseCase;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.domain.user.User;

/**
 * Resolves the module grants effective for an authenticated user (issue #1683).
 */
@Component
public class RoleModuleGrantService {

    private final QueryUsersUseCase users;

    private final QueryRolesUseCase roles;

    public RoleModuleGrantService(QueryUsersUseCase users, QueryRolesUseCase roles) {
        this.users = users;
        this.roles = roles;
    }

    public Set<String> grantedModules(String username) {
        return users.findByName(username)
                .filter(User::active)
                .map(this::modulesFor)
                .orElseGet(Set::of);
    }

    public boolean isGranted(String username, String module) {
        if (module == null || module.isBlank()) {
            return true;
        }
        String normalized = module.trim().toLowerCase(Locale.ROOT);
        return grantedModules(username).contains(normalized);
    }

    private Set<String> modulesFor(User user) {
        if (user.roleId() == null) {
            return StandardRoleModules.fallbackForUserType(user.type());
        }
        return roles.findById(user.roleId())
                .map(role -> normalize(role.modules()))
                .orElseGet(() -> StandardRoleModules.fallbackForUserType(user.type()));
    }

    private static Set<String> normalize(List<String> modules) {
        Set<String> normalized = new HashSet<>();
        for (String module : modules) {
            if (module != null && !module.isBlank()) {
                normalized.add(module.trim().toLowerCase(Locale.ROOT));
            }
        }
        return Set.copyOf(normalized);
    }
}
