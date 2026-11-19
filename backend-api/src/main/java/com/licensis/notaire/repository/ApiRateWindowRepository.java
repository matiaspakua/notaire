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
}
