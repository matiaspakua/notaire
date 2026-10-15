package com.licensis.notaire.application.port.in.user;

import com.licensis.notaire.domain.user.User;

/**
 * Credential check for login. Lockout, JWT, cookies, and metrics stay in the HTTP adapter
 * (issue #1731).
 */
public interface AuthenticateUserUseCase {

    enum Failure {
        NOT_FOUND,
        BAD_CREDENTIALS,
        INACTIVE
    }

    sealed interface Result permits Result.Success, Result.Failed {
        record Success(User user) implements Result {}

        record Failed(Failure failure) implements Result {}
    }

    Result authenticate(String username, String rawPassword);
}
