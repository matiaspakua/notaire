package com.licensis.notaire.application.port.in.user;

import com.licensis.notaire.domain.user.User;

public interface ManageUsersUseCase {

    record CreateUserCommand(String name, String rawPassword, String type, boolean active) {}

    record UpdateUserCommand(String name, String rawPassword, String type, boolean active) {}

    User create(CreateUserCommand command);

    void update(int id, UpdateUserCommand command);

    void delete(int id);
}
