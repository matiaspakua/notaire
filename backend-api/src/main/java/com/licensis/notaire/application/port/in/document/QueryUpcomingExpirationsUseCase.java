package com.licensis.notaire.application.port.in.document;

import com.licensis.notaire.dto.DtoUpcomingExpiration;
import java.time.LocalDate;
import java.util.List;

/**
 * Inbound port for CU42 — upcoming submitted-document expirations (issue #1866).
 */
public interface QueryUpcomingExpirationsUseCase {

    List<DtoUpcomingExpiration> findUpcoming(Integer days, LocalDate today);
}
