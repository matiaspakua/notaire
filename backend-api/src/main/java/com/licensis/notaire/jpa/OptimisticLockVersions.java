package com.licensis.notaire.jpa;

import java.util.Objects;

/**
 * Optimistic-lock version compare for legacy JpaControllers.
 * Boxed {@link Integer} values must not use {@code ==}/{@code !=} (java:S4973 / SpotBugs
 * RC_REF_COMPARISON) — issue #1758.
 */
public final class OptimisticLockVersions {

    private OptimisticLockVersions() {}

    /** True when the two versions are not the same value (null-safe). */
    public static boolean differ(Integer left, Integer right) {
        return !Objects.equals(left, right);
    }
}
