package com.licensis.notaire.domain.deed;

/**
 * Result of validating a proposed escritura número against the correlativo
 * expected for its protocolo, año and escribano (CU86).
 */
public enum DeedNumberingValidationResult {
    OK,
    DUPLICATE,
    SKIP_UNJUSTIFIED,
    SKIP_JUSTIFIED
}
