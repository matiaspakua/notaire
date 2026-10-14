package com.licensis.notaire.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller read handler that returns personal data, so {@link AuditAspect} writes
 * one PII-free audit row per request (issue #1635, SR-08). Reads stay unaudited by default.
 *
 * <p>{@link #aggregated()} is for list endpoints that UI pickers call over and over: a user's
 * first call in a window is audited and the repeats are not (issue #1664). Searches and lookups
 * by id are never aggregated.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditPersonalDataRead {

    boolean aggregated() default false;
}
