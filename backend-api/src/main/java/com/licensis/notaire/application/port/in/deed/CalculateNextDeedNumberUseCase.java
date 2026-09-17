package com.licensis.notaire.application.port.in.deed;

import com.licensis.notaire.business.Person;

/**
 * Inbound port for calculating the next deed sequence number.
 * Used by domain to determine what the next correlativo should be
 * for a given notary, year, and folio type.
 */
public interface CalculateNextDeedNumberUseCase {

    /**
     * Calculate the next expected sequence number for a deed.
     *
     * @param notary the notary person (contains personId)
     * @param year the year
     * @param isAuxiliary true if for auxiliary protocol, false for principal
     * @return the next sequence number (starting at 1 if none exist yet)
     */
    int calculateNextSequenceNumber(Person notary, int year, boolean isAuxiliary);
}
