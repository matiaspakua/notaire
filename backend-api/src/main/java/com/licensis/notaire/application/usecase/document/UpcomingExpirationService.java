package com.licensis.notaire.application.usecase.document;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.dto.DtoUpcomingExpiration;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU42 - Informar próximos vencimientos: lists the unreleased submitted documents whose due date falls between
 * today and today plus a window of days, both inclusive.
 */
@Service
@Transactional(readOnly = true)
public class UpcomingExpirationService {

    public static final int DEFAULT_WINDOW_DAYS = 30;
    public static final int MAX_WINDOW_DAYS = 365;

    private final SubmittedDocumentRepository repository;

    public UpcomingExpirationService(SubmittedDocumentRepository repository) {
        this.repository = repository;
    }

    /**
     * @param days window in days between 1 and {@value #MAX_WINDOW_DAYS}, or null for the default
     * @param today the first day of the window
     * @return the upcoming expirations ordered by due date
     * @throws BusinessValidationException if the window is outside the allowed range
     */
    public List<DtoUpcomingExpiration> findUpcoming(Integer days, LocalDate today) {
        int window = days == null ? DEFAULT_WINDOW_DAYS : days;
        if (window < 1 || window > MAX_WINDOW_DAYS) {
            throw new BusinessValidationException(
                    "La ventana de vencimientos debe estar entre 1 y " + MAX_WINDOW_DAYS + " días");
        }
        return repository.findUpcomingExpirations(toDate(today), toDate(today.plusDays(window))).stream()
                .map(document -> toDto(document, today))
                .toList();
    }

    private static DtoUpcomingExpiration toDto(SubmittedDocument document, LocalDate today) {
        Optional<DeedManagement> management = Optional.ofNullable(document.getFkIdProcedure())
                .map(procedure -> procedure.getFkIdManagement());
        LocalDate dateDue = toLocalDate(document.getDateDue());
        return new DtoUpcomingExpiration(
                document.getIdSubmittedDocument(),
                document.getName(),
                management.map(DeedManagement::getNumber).orElse(null),
                management.map(DeedManagement::getEncabezado).orElse(null),
                document.getPrepared(),
                toLocalDate(document.getDateEntry()),
                toLocalDate(document.getDateExit()),
                document.getCardNumber(),
                Boolean.TRUE.equals(document.getFlagged()),
                document.getAmountToPay(),
                toLocalDate(document.getDatePayment()),
                toLocalDate(document.getDateReleased()),
                document.getNotes(),
                dateDue,
                ChronoUnit.DAYS.between(today, dateDue));
    }

    private static Date toDate(LocalDate day) {
        return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return new Date(date.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
