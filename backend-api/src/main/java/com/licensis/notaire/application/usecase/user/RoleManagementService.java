package com.licensis.notaire.application.usecase.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.user.ManageRolesUseCase;
import com.licensis.notaire.application.port.out.user.RoleCatalogPort;
import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.domain.user.Role;
import com.licensis.notaire.domain.user.RoleNameConflictException;
import com.licensis.notaire.domain.user.RoleNotFoundException;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.domain.user.UserNotFoundException;

@Service
@Transactional
public class RoleManagementService implements ManageRolesUseCase {

    private final RoleCatalogPort roles;
    private final UserCatalogPort users;

    public RoleManagementService(RoleCatalogPort roles, UserCatalogPort users) {
        this.roles = roles;
        this.users = users;
    }

    @Override
    public Role create(CreateRoleCommand command) {
        if (roles.findByName(command.name()).isPresent()) {
            throw new RoleNameConflictException(command.name());
        }
        return roles.save(Role.create(command.name(), command.description(), command.active(), command.modules()));
    }

    @Override
    public Role update(int id, UpdateRoleCommand command) {
        Role existing = roles.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
        return roles.save(
                existing.changeTo(command.name(), command.description(), command.active(), command.modules()));
    }

    @Override
    public void delete(int id) {
        if (!roles.existsById(id)) {
            throw new RoleNotFoundException(id);
        }
        if (!roles.deleteById(id)) {
            throw new RoleNotFoundException(id);
        }
    }

    @Override
    public void assignRoleToUser(int roleId, int userId) {
        Role role = roles.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
        User user = users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        users.save(user.withRole(role.id(), role.name()));
    }

    @Override
    public void unassignRoleFromUser(int userId) {
        User user = users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        users.save(user.withRole(null, null));
    }
}
