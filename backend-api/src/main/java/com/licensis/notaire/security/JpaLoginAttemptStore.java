package com.licensis.notaire.security;

import com.licensis.notaire.business.LoginAttempt;
import com.licensis.notaire.repository.LoginAttemptRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;

/**
 * Database-backed lockout state shared by every backend instance (issue #1634). Writes run in
 * their own transaction so a failed login persists whatever happens to the request, and the
 * counter is incremented by a single UPDATE so concurrent failures add up.
 */
@Component
public class JpaLoginAttemptStore implements LoginAttemptStore {

    private static final int INSERT_RACE_RETRIES = 2;

    private final LoginAttemptRepository repository;
    private final TransactionTemplate transaction;

    public JpaLoginAttemptStore(LoginAttemptRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Optional<Instant> lockedUntil(String username) {
        return transaction.execute(status -> repository.findById(username).map(LoginAttempt::getLockedUntil));
    }

    @Override
    public void recordFailure(String username, int maxAttempts, Instant now, Instant lockUntil) {
        for (int attempt = 1; ; attempt++) {
            try {
                transaction.executeWithoutResult(status -> {
                    if (repository.incrementFailure(username, now, maxAttempts, lockUntil) == 0) {
                        LoginAttempt row = new LoginAttempt(username, now);
                        row.recordFailure(maxAttempts, now, lockUntil);
                        repository.saveAndFlush(row);
                    }
                });
                return;
            } catch (DataIntegrityViolationException insertRace) {
                if (attempt >= INSERT_RACE_RETRIES) {
                    throw insertRace;
                }
            }
        }
    }

    @Override
    public void clear(String username) {
        transaction.executeWithoutResult(status -> repository.deleteById(username));
    }

    @Override
    public void purgeIdleSince(Instant cutoff) {
        transaction.executeWithoutResult(status -> repository.deleteIdleSince(cutoff));
    }
}
