package com.licensis.notaire.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.licensis.notaire.security.ApiRateWindow;

@Repository
public interface ApiRateWindowRepository extends JpaRepository<ApiRateWindow, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ApiRateWindow w SET "
            + "w.requestCount = CASE WHEN w.windowStart <= :resetAt THEN 1 ELSE w.requestCount + 1 END, "
            + "w.windowStart = CASE WHEN w.windowStart <= :resetAt THEN :now ELSE w.windowStart END "
            + "WHERE w.bucketKey = :key")
    int advance(@Param("key") String key, @Param("now") Instant now, @Param("resetAt") Instant resetAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ApiRateWindow w WHERE w.windowStart <= :resetAt")
    int purgeExpired(@Param("resetAt") Instant resetAt);

    /**
     * Insert a new window row; silent no-op when {@code bucket_key} already exists
     * (avoids the primary-key collision race, issue #2389).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO api_rate_windows (bucket_key, window_start, request_count) "
            + "VALUES (:key, :now, 0) "
            + "ON CONFLICT DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(@Param("key") String key, @Param("now") Instant now);
}
