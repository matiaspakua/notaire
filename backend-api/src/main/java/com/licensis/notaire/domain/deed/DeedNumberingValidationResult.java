package com.licensis.notaire.domain.deed;

/**
 * Result of validating a proposed deed number against the correlativo
 * expected for its protocolo, year and notary (CU86).
 */
public enum DeedNumberingValidationResult {
    OK,
    DUPLICATE,
    SKIP_UNJUSTIFIED,
    SKIP_JUSTIFIED
}
