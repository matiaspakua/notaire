package com.licensis.notaire.application.port.out.deed;

import java.util.Optional;

/**
 * Outbound port for deed numbering persistence queries.
 * Abstracts the details of how deed number metadata is queried.
 */
public interface DeedNumberingPort {

    /**
     * Find the maximum deed number for a given notary, year, and folio type.
     *
     * @param notaryId the notary person ID
     * @param year     the year
     * @param auxiliary true for auxiliary folio type, false for principal
     * @param excludedDeedId deed ID to exclude from search (for updates), or null
     * @return the maximum deed number found, or empty if none exists
     */
    Optional<Integer> findMaxNumberDeedByNotaryYearAndType(
            Integer notaryId, int year, boolean auxiliary, Integer excludedDeedId);

    /**
     * Check if a deed number already exists for a given notary, year, and folio type.
     *
     * @param number the deed number to check
     * @param notaryId the notary person ID
     * @param year the year
     * @param auxiliary true for auxiliary folio type, false for principal
     * @param excludedDeedId deed ID to exclude from search (for updates), or null
     * @return true if the number exists, false otherwise
     */
    boolean existsNumberDeedByNotaryYearAndType(
            int number, Integer notaryId, int year, boolean auxiliary, Integer excludedDeedId);
}
