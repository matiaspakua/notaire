package com.licensis.notaire.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller read handler that returns personal data, so {@link AuditAspect} writes
 * one PII-free audit row per request (issue #1635, SR-08). Reads stay unaudited by default.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditPersonalDataRead {
}
