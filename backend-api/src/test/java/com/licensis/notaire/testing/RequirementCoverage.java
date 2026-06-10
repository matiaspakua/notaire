package com.licensis.notaire.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents which Use Case IDs (e.g. "CU01", "CU45") a test class covers.
 * Used to generate a requirements traceability matrix.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirementCoverage {
    String[] value();
}
