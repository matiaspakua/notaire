package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.licensis.notaire.application.usecase.document.UpcomingExpirationService;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.dto.DtoUpcomingExpiration;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@RequirementCoverage({"CU42"})
@DisplayName("UpcomingExpirationService — CU42 próximos vencimientos")
@ExtendWith(MockitoExtension.class)
class UpcomingExpirationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 4);

    @Mock
    private SubmittedDocumentRepository repository;

    @InjectMocks
    private UpcomingExpirationService service;

    private static Date toDate(LocalDate day) {
        return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static SubmittedDocument documentDueIn(int days) {
        DeedManagement management = new DeedManagement();
        management.setNumber(7);
        management.setEncabezado("Compraventa Pérez");
        Procedure procedure = new Procedure();
        procedure.setFkIdManagement(management);

        SubmittedDocument document = new SubmittedDocument();
        document.setIdSubmittedDocument(11);
        document.setName("Certificado de dominio");
        document.setExpires(true);
        document.setPrepared(true);
        document.setFlagged(false);
        document.setCardNumber(33);
        document.setAmountToPay(new BigDecimal("1500.50"));
        document.setNotes("Pedir copia");
        document.setDateEntry(toDate(TODAY.minusDays(5)));
        document.setDateDue(toDate(TODAY.plusDays(days)));
        document.setFkIdProcedure(procedure);
        return document;
    }

    @Test
    @DisplayName("Should map a document due inside the window with its CU42 data and days remaining")
    void shouldMapDocumentDueInsideTheWindow() {
        when(repository.findUpcomingExpirations(any(Date.class), any(Date.class)))
                .thenReturn(List.of(documentDueIn(10)));

        List<DtoUpcomingExpiration> result = service.findUpcoming(30, TODAY);

        assertThat(result).singleElement().satisfies(row -> {
            assertThat(row.idSubmittedDocument()).isEqualTo(11);
            assertThat(row.documentName()).isEqualTo("Certificado de dominio");
            assertThat(row.managementNumber()).isEqualTo(7);
            assertThat(row.managementHeading()).isEqualTo("Compraventa Pérez");
            assertThat(row.prepared()).isTrue();
            assertThat(row.cardNumber()).isEqualTo(33);
            assertThat(row.amountToPay()).isEqualByComparingTo("1500.50");
            assertThat(row.dateDue()).isEqualTo(TODAY.plusDays(10));
            assertThat(row.daysRemaining()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("Should leave management data empty for an autonomous document without trámite")
    void shouldAllowDocumentWithoutProcedure() {
        SubmittedDocument autonomous = documentDueIn(3);
        autonomous.setFkIdProcedure(null);
        when(repository.findUpcomingExpirations(any(Date.class), any(Date.class))).thenReturn(List.of(autonomous));

        List<DtoUpcomingExpiration> result = service.findUpcoming(30, TODAY);

        assertThat(result).singleElement().satisfies(row -> {
            assertThat(row.managementNumber()).isNull();
            assertThat(row.managementHeading()).isNull();
        });
    }

    @Test
    @DisplayName("Should query from today to today plus the window")
    void shouldQueryTheInclusiveWindow() {
        when(repository.findUpcomingExpirations(any(Date.class), any(Date.class))).thenReturn(List.of());

        service.findUpcoming(15, TODAY);

        ArgumentCaptor<Date> from = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> to = ArgumentCaptor.forClass(Date.class);
        verify(repository).findUpcomingExpirations(from.capture(), to.capture());
        assertThat(from.getValue()).isEqualTo(toDate(TODAY));
        assertThat(to.getValue()).isEqualTo(toDate(TODAY.plusDays(15)));
    }

    @Test
    @DisplayName("Should use a 30-day window when none is given")
    void shouldDefaultToThirtyDays() {
        when(repository.findUpcomingExpirations(any(Date.class), any(Date.class))).thenReturn(List.of());

        service.findUpcoming(null, TODAY);

        ArgumentCaptor<Date> to = ArgumentCaptor.forClass(Date.class);
        verify(repository).findUpcomingExpirations(any(Date.class), to.capture());
        assertThat(to.getValue()).isEqualTo(toDate(TODAY.plusDays(30)));
    }

    @Test
    @DisplayName("Should reject a window smaller than 1 or larger than 365 days")
    void shouldRejectAnInvalidWindow() {
        assertThatThrownBy(() -> service.findUpcoming(0, TODAY)).isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> service.findUpcoming(366, TODAY)).isInstanceOf(BusinessValidationException.class);
    }
}
