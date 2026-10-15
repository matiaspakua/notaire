package com.licensis.notaire.application.usecase.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase;
import com.licensis.notaire.application.port.out.user.PasswordHasherPort;
import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.domain.user.User;

/**
 * Login credential check with BCrypt and transparent MD5→BCrypt upgrade (#554 / #1731).
 */
@Service
@Transactional
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserCatalogPort catalog;
    private final PasswordHasherPort passwords;

    public AuthenticateUserService(UserCatalogPort catalog, PasswordHasherPort passwords) {
        this.catalog = catalog;
        this.passwords = passwords;
    }

    @Override
    public Result authenticate(String username, String rawPassword) {
        User match = null;
        for (User user : catalog.findAll()) {
            if (user.name() != null && user.name().equalsIgnoreCase(username)) {
                match = user;
                break;
            }
        }
        if (match == null) {
            return new Result.Failed(Failure.NOT_FOUND);
        }
        if (!passwordMatches(rawPassword, match)) {
            return new Result.Failed(Failure.BAD_CREDENTIALS);
        }
        if (!match.active()) {
            return new Result.Failed(Failure.INACTIVE);
        }
        // Reload so callers see upgraded hash / current identity after MD5 migration.
        User current = catalog.findById(match.id()).orElse(match);
        return new Result.Success(current);
    }

    private boolean passwordMatches(String rawPassword, User user) {
        String storedHash = user.passwordHash();
        if (passwords.isBcryptHash(storedHash)) {
            return passwords.matchesBcrypt(rawPassword, storedHash);
        }
        if (!passwords.legacyMd5Hash(rawPassword).equals(storedHash)) {
            return false;
        }
        catalog.save(user.withPasswordHash(passwords.encode(rawPassword)));
        return true;
    }
}
