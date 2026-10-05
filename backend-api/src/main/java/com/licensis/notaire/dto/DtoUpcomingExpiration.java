package com.licensis.notaire.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CU42 - A submitted document approaching its expiration date, with the data the report lists.
 */
public record DtoUpcomingExpiration(
        Integer idSubmittedDocument,
        String documentName,
        Integer managementNumber,
        String managementHeading,
        boolean prepared,
        LocalDate dateEntry,
        LocalDate dateExit,
        Integer cardNumber,
        boolean observed,
        BigDecimal amountToPay,
        LocalDate datePayment,
        LocalDate dateReleased,
        String notes,
        LocalDate dateDue,
        long daysRemaining) {
}
