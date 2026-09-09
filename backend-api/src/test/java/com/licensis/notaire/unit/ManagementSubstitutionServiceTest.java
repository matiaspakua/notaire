package com.licensis.notaire.unit;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.repository.SubstitutionRepository;
import com.licensis.notaire.service.ManagementSubstitutionService;
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
@DisplayName("GestionSuplenciaService unit tests")
@ExtendWith(MockitoExtension.class)
class ManagementSubstitutionServiceTest {

    @Mock
    private SubstitutionRepository substitutionRepository;

    private ManagementSubstitutionService managementSubstitutionService;

    private Person notarySolicitado;
    private Person suplente;
    private Date dateManagement;

    @BeforeEach
    void setUp() {
        managementSubstitutionService = new ManagementSubstitutionService(substitutionRepository);

        notarySolicitado = new Person();
        notarySolicitado.setPersonId(10);
        notarySolicitado.setFirstName("Escribano");
        notarySolicitado.setLastName("Solicitado");

        suplente = new Person();
        suplente.setPersonId(20);
        suplente.setFirstName("Escribano");
        suplente.setLastName("Suplente");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2026, Calendar.JANUARY, 15, 0, 0, 0);
        dateManagement = calendar.getTime();
    }

    @Test
    @DisplayName("Should assign requested notary when no active suplencia exists")
    void shouldAssignRequestedNotaryWhenNoActiveSubstitution() {
        when(substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        eq(notarySolicitado.getPersonId()), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        ManagementSubstitutionService.NotaryAsignado resultado =
                managementSubstitutionService.resolverNotary(notarySolicitado, dateManagement);

        assertThat(resultado.notary()).isEqualTo(notarySolicitado);
        assertThat(resultado.substitutionAplicada()).isNull();
    }

    @Test
    @DisplayName("Should assign suplente when notary has an active suplencia")
    void shouldAssignSuplenteWhenNotaryHasActiveSubstitution() {
        Substitution substitutionActiva = new Substitution(1, dateManagement, dateManagement);
        substitutionActiva.setFkIdSubstituted(notarySolicitado);
        substitutionActiva.setFkIdSubstitute(suplente);
        when(substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        eq(notarySolicitado.getPersonId()), any(Date.class), any(Date.class)))
                .thenReturn(List.of(substitutionActiva));

        ManagementSubstitutionService.NotaryAsignado resultado =
                managementSubstitutionService.resolverNotary(notarySolicitado, dateManagement);

        assertThat(resultado.notary()).isEqualTo(suplente);
        assertThat(resultado.substitutionAplicada()).isEqualTo(substitutionActiva);
        verify(substitutionRepository)
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        eq(notarySolicitado.getPersonId()), any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("Should record the redirection identifying requested and assigned escribanos")
    void shouldRecordRedirectionInNotes() {
        String observacion = managementSubstitutionService.observacionRedireccion(notarySolicitado, suplente);

        assertThat(observacion)
                .contains(notarySolicitado.getFirstName())
                .contains(notarySolicitado.getLastName())
                .contains(suplente.getFirstName())
                .contains(suplente.getLastName());
    }
}
