package com.licensis.notaire.application.port.in.user;

import java.util.List;

import com.licensis.notaire.domain.user.Role;

public interface ManageRolesUseCase {

    record CreateRoleCommand(String name, String description, boolean active, List<String> modules) {}

    record UpdateRoleCommand(String name, String description, boolean active, List<String> modules) {}

    Role create(CreateRoleCommand command);

    Role update(int id, UpdateRoleCommand command);

    void delete(int id);

    void assignRoleToUser(int roleId, int userId);

    void unassignRoleFromUser(int userId);
}
