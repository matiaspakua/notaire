package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.RegistrationDraftRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.service.RegistrationDraftService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU82"})
@DisplayName("MinutaInscripcionService Tests")
@ExtendWith(MockitoExtension.class)
class RegistrationDraftServiceTest {

    @Mock
    private RegistrationDraftRepository registrationDraftRepository;

    @Mock
    private DeedRepository deedRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    private RegistrationDraftService registrationDraftService;

    private Deed deed;
    private Property property;
    private Procedure procedure;

    @BeforeEach
    void setUp() {
        registrationDraftService = new RegistrationDraftService(registrationDraftRepository, deedRepository,
                procedureRepository);

        deed = new Deed();
        deed.setIdDeed(1);
        deed.setStatus(BusinessConstants.DeedFIRMADA);

        property = new Property();
        property.setIdProperty(1);
        property.setCadastralDesignation("123-456-789");
        property.setFiscalAppraisal(1000f);
        property.setAddress("Calle Falsa 123");
        property.setRegistrationNumber("M-1");
        property.setVolumeFolioLandRecord("T1-F2-FN3");
        property.setBoundaries("Norte, Sur, Este, Oeste");

        procedure = new Procedure();
        procedure.setIdProcedure(10);
        procedure.setFkIdProperty(property);
        procedure.setFkIdDeed(deed);
    }

    @Test
    @DisplayName("Should generate minuta en status Generada with complete data")
    void shouldGenerateDraftWhenDataIsComplete() {
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));
        when(procedureRepository.findByFkIdDeedIdDeed(1)).thenReturn(List.of(procedure));
        when(registrationDraftRepository.findTopByOrderByNumberDesc()).thenReturn(Optional.empty());
        when(registrationDraftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationDraft draft = registrationDraftService.generar(1);

        assertThat(draft.getNumber()).isEqualTo(1);
        assertThat(draft.getStatus()).isEqualTo(BusinessConstants.RegistrationDraftGENERADA);
        assertThat(draft.getFkIdDeed()).isEqualTo(deed);
        assertThat(draft.getDateGeneration()).isNotNull();
    }

    @Test
    @DisplayName("Should reject generation when catastral/registral data is incomplete")
    void shouldRejectGenerationWhenDataIsIncomplete() {
        property.setRegistrationNumber(null);
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));
        when(procedureRepository.findByFkIdDeedIdDeed(1)).thenReturn(List.of(procedure));

        assertThatThrownBy(() -> registrationDraftService.generar(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("matrícula");
    }

    @Test
    @DisplayName("Should reject generation when deed is not firmada")
    void shouldRejectGenerationWhenDeedNotFirmada() {
        deed.setStatus(BusinessConstants.DeedSINFIRMAR);
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));

        assertThatThrownBy(() -> registrationDraftService.generar(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("firmada");
    }

    @Test
    @DisplayName("Should throw when deed does not exist")
    void shouldThrowWhenDeedNotFound() {
        when(deedRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationDraftService.generar(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should register presentacion and transition to Presentado")
    void shouldRegisterSubmission() {
        RegistrationDraft draft = buildDraft(1, BusinessConstants.RegistrationDraftGENERADA);
        when(registrationDraftRepository.findById(1)).thenReturn(Optional.of(draft));
        when(registrationDraftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationDraft result = registrationDraftService.presentar(1, new Date(), "ENT-123");

        assertThat(result.getStatus()).isEqualTo(BusinessConstants.RegistrationDraftPRESENTADA);
        assertThat(result.getRegistryEntryNumber()).isEqualTo("ENT-123");
        assertThat(result.getDateSubmission()).isNotNull();
    }

    @Test
    @DisplayName("Should reject presentacion when minuta is not en status Generada")
    void shouldRejectSubmissionWhenNotGenerada() {
        RegistrationDraft draft = buildDraft(1, BusinessConstants.RegistrationDraftPRESENTADA);
        when(registrationDraftRepository.findById(1)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> registrationDraftService.presentar(1, new Date(), "ENT-123"))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should register observacion and transition to Observado")
    void shouldRegisterObservacion() {
        RegistrationDraft draft = buildDraft(1, BusinessConstants.RegistrationDraftPRESENTADA);
        when(registrationDraftRepository.findById(1)).thenReturn(Optional.of(draft));
        when(registrationDraftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationDraft result = registrationDraftService.observar(1, "Falta plano de mensura", new Date());

        assertThat(result.getStatus()).isEqualTo(BusinessConstants.RegistrationDraftOBSERVADA);
        assertThat(result.getRegistryNotes()).isEqualTo("Falta plano de mensura");
        assertThat(result.getDateCorrection()).isNotNull();
    }

    @Test
    @DisplayName("Should reject observacion when minuta is not presentada")
    void shouldRejectObservacionWhenNotPresentada() {
        RegistrationDraft draft = buildDraft(1, BusinessConstants.RegistrationDraftGENERADA);
        when(registrationDraftRepository.findById(1)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> registrationDraftService.observar(1, "Observación", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should register inscripcion definitiva and transition to Inscripto")
    void shouldRegisterRegistrationDefinitiva() {
        RegistrationDraft draft = buildDraft(1, BusinessConstants.RegistrationDraftPRESENTADA);
        when(registrationDraftRepository.findById(1)).thenReturn(Optional.of(draft));
        when(registrationDraftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationDraft result = registrationDraftService.inscribir(1, new Date(), "INS-456");

        assertThat(result.getStatus()).isEqualTo(BusinessConstants.RegistrationDraftRegistered);
        assertThat(result.getFinalRegistrationNumber()).isEqualTo("INS-456");
        assertThat(result.getDateReception()).isNotNull();
    }

    @Test
    @DisplayName("Should reject inscripcion definitiva when minuta is not presentada")
    void shouldRejectRegistrationWhenNotPresentada() {
        RegistrationDraft draft = buildDraft(1, BusinessConstants.RegistrationDraftOBSERVADA);
        when(registrationDraftRepository.findById(1)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> registrationDraftService.inscribir(1, new Date(), "INS-456"))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should throw when minuta does not exist")
    void shouldThrowWhenDraftNotFound() {
        when(registrationDraftRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationDraftService.presentar(999, new Date(), "ENT-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private RegistrationDraft buildDraft(Integer id, String status) {
        RegistrationDraft draft = new RegistrationDraft();
        draft.setIdRegistrationDraft(id);
        draft.setNumber(1);
        draft.setStatus(status);
        draft.setFkIdDeed(deed);
        return draft;
    }
}
