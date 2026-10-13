package com.licensis.notaire.repository;

import com.licensis.notaire.business.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    @Modifying
    @Query("UPDATE LoginAttempt a SET a.failedCount = a.failedCount + 1, a.updatedAt = :now, "
            + "a.lockedUntil = CASE WHEN a.failedCount + 1 >= :maxAttempts THEN :lockUntil ELSE a.lockedUntil END "
            + "WHERE a.username = :username")
    int incrementFailure(@Param("username") String username, @Param("now") Instant now,
            @Param("maxAttempts") int maxAttempts, @Param("lockUntil") Instant lockUntil);

    @Modifying
    @Query("DELETE FROM LoginAttempt a WHERE a.updatedAt < :cutoff")
    int deleteIdleSince(@Param("cutoff") Instant cutoff);
}
