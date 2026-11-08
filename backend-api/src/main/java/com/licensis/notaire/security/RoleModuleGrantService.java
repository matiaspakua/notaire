package com.licensis.notaire.security;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.licensis.notaire.business.Role;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.UserRepository;

/**
 * Resolves the module grants effective for an authenticated user (issue #1683).
 */
@Component
public class RoleModuleGrantService {

    private final UserRepository userRepository;

    public RoleModuleGrantService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<String> grantedModules(String username) {
        return userRepository.findByName(username)
                .filter(User::getStatus)
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
        Role role = user.getRole();
        if (role != null) {
            return normalize(role.getModulos());
        }
        return StandardRoleModules.fallbackForUserType(user.getType());
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
