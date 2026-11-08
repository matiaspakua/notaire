package com.licensis.notaire.application.usecase.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.user.ManageUsersUseCase;
import com.licensis.notaire.application.port.out.user.PasswordHasherPort;
import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.domain.user.UserNotFoundException;

@Service
@Transactional
public class UserManagementService implements ManageUsersUseCase {

    private final UserCatalogPort catalog;
    private final PasswordHasherPort passwords;

    public UserManagementService(UserCatalogPort catalog, PasswordHasherPort passwords) {
        this.catalog = catalog;
        this.passwords = passwords;
    }

    @Override
    public User create(CreateUserCommand command) {
        String raw = command.rawPassword();
        String hash = raw != null && !raw.isEmpty() ? passwords.encode(raw) : "";
        return catalog.save(User.create(command.name(), hash, command.active(), command.type()));
    }

    @Override
    public void update(int id, UpdateUserCommand command) {
        User existing = catalog.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        String raw = command.rawPassword();
        String newHash = raw != null && !raw.isEmpty() ? passwords.encode(raw) : null;
        catalog.save(existing.withCredentials(command.name(), newHash, command.active(), command.type()));
    }

    @Override
    public void delete(int id) {
        if (!catalog.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        if (!catalog.deleteById(id)) {
            throw new UserNotFoundException(id);
        }
    }
}
