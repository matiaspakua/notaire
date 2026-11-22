package com.licensis.notaire.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.licensis.notaire.repository.ApiRateWindowRepository;

/**
 * Database window shared by every API instance (issue #2010). The count moves
 * by one UPDATE so concurrent calls add up, in its own transaction so a later
 * rollback of the request does not forget the call. The insert uses
 * {@code INSERT ... ON CONFLICT DO NOTHING} so concurrent first requests never
 * collide on the primary key (issue #2389).
 */
@Component
public class JpaApiRateWindowStore implements ApiRateWindowStore {

    private final ApiRateWindowRepository repository;
    private final TransactionTemplate transaction;

    public JpaApiRateWindowStore(ApiRateWindowRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public boolean tryAcquire(String key, int maxRequests, Duration window, Instant now) {
        String bucket = InMemoryApiRateWindowStore.bucket(key);
        Instant resetAt = now.minus(window);
        Boolean allowed = transaction.execute(status -> {
            repository.purgeExpired(resetAt);
            repository.insertIfAbsent(bucket, now);
            repository.advance(bucket, now, resetAt);
            int count = repository.findById(bucket).orElseThrow().getRequestCount();
            return count <= maxRequests;
        });
        return Boolean.TRUE.equals(allowed);
    }

    @Override
    public long retryAfterSeconds(String key, Duration window, Instant now) {
        String bucket = InMemoryApiRateWindowStore.bucket(key);
        Long seconds = transaction.execute(status -> repository.findById(bucket)
                .map(row -> {
                    long elapsed = Duration.between(row.getWindowStart(), now).getSeconds();
                    return Math.max(1L, window.toSeconds() - elapsed);
                })
                .orElse(window.toSeconds()));
        return seconds == null ? window.toSeconds() : seconds;
    }
}
