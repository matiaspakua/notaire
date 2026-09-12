package com.licensis.notaire.service;

/**
 * Result of validating a proposed escritura número against the correlativo
 * expected for its protocolo, año and escribano (CU86).
 */
public enum NumberingValidationResult {
    OK,
    DUPLICATE,
    SKIP_UNJUSTIFIED,
    SKIP_JUSTIFIED
}
