package com.licensis.notaire.unit;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.repository.SubstitutionRepository;
import com.licensis.notaire.application.usecase.management.ManagementSubstitutionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU22", "CU59"})
@DisplayName("ManagementSubstitutionService unit tests")
@ExtendWith(MockitoExtension.class)
class ManagementSubstitutionServiceTest {

    @Mock
    private SubstitutionRepository substitutionRepository;

    private ManagementSubstitutionService managementSubstitutionService;

    private Person requestedNotary;
    private Person substitute;
    private Date managementDate;

    @BeforeEach
    void setUp() {
        managementSubstitutionService = new ManagementSubstitutionService(substitutionRepository);

        requestedNotary = new Person();
        requestedNotary.setPersonId(10);
        requestedNotary.setFirstName("Requested");
        requestedNotary.setLastName("Notary");

        substitute = new Person();
        substitute.setPersonId(20);
        substitute.setFirstName("Substitute");
        substitute.setLastName("Notary");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2026, Calendar.JANUARY, 15, 0, 0, 0);
        managementDate = calendar.getTime();
    }

    @Test
    @DisplayName("Should assign requested notary when no active substitution exists")
    void shouldAssignRequestedNotaryWhenNoActiveSubstitution() {
        when(substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        eq(requestedNotary.getPersonId()), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        ManagementSubstitutionService.AssignedNotary result =
                managementSubstitutionService.resolveNotary(requestedNotary, managementDate);

        assertThat(result.notary()).isEqualTo(requestedNotary);
        assertThat(result.appliedSubstitution()).isNull();
    }

    @Test
    @DisplayName("Should assign substitute when notary has an active substitution")
    void shouldAssignSuplenteWhenNotaryHasActiveSubstitution() {
        Substitution activeSubstitution = new Substitution(1, managementDate, managementDate);
        activeSubstitution.setFkIdSubstituted(requestedNotary);
        activeSubstitution.setFkIdSubstitute(substitute);
        when(substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        eq(requestedNotary.getPersonId()), any(Date.class), any(Date.class)))
                .thenReturn(List.of(activeSubstitution));

        ManagementSubstitutionService.AssignedNotary result =
                managementSubstitutionService.resolveNotary(requestedNotary, managementDate);

        assertThat(result.notary()).isEqualTo(substitute);
        assertThat(result.appliedSubstitution()).isEqualTo(activeSubstitution);
        verify(substitutionRepository)
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        eq(requestedNotary.getPersonId()), any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("Should record the redirection identifying requested and assigned notaries")
    void shouldRecordRedirectionInNotes() {
        String note = managementSubstitutionService.redirectionNote(requestedNotary, substitute);

        assertThat(note)
                .contains("redirected by active substitution")
                .contains(requestedNotary.getFirstName())
                .contains(requestedNotary.getLastName())
                .contains(substitute.getFirstName())
                .contains(substitute.getLastName());
    }
}
