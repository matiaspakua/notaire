package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.licensis.notaire.application.usecase.management.ManagementCaseSummaryService;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.dto.DtoCaseDeed;
import com.licensis.notaire.dto.DtoCaseDocument;
import com.licensis.notaire.dto.DtoCaseTestimony;
import com.licensis.notaire.dto.DtoManagementCaseSummary;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@RequirementCoverage({"CU07", "CU11", "CU12", "CU70"})
@DisplayName("ManagementCaseSummaryService — escrituras, testimonios and copias of a gestión")
@ExtendWith(MockitoExtension.class)
class ManagementCaseSummaryServiceTest {

    @Mock
    private DeedManagementRepository managementRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private SubmittedDocumentRepository submittedDocumentRepository;

    @InjectMocks
    private ManagementCaseSummaryService service;

    private static DeedManagement management() {
        DeedManagement management = new DeedManagement();
        management.setIdManagement(4);
        management.setNumber(1001);
        management.setEncabezado("Compraventa Pérez");
        return management;
    }

    private static TestimonyMovement movement(int id, Date exit, boolean registered) {
        TestimonyMovement movement = new TestimonyMovement();
        movement.setIdTestimonyMovement(id);
        movement.setDateEntry(new Date());
        movement.setDateExit(exit);
        movement.setRegistered(registered);
        return movement;
    }

    private static Procedure procedureWithTestimony(Testimony testimony) {
        Deed deed = new Deed(9, 55, new Date(), "cuerpo", "Firmada");
        deed.setTestimonyList(List.of(testimony));
        Procedure procedure = new Procedure(1);
        procedure.setFkIdDeed(deed);
        return procedure;
    }

    private static Testimony testimony(List<TestimonyMovement> movements, int copies) {
        Testimony testimony = new Testimony(21, 210, false);
        testimony.setVerified(true);
        testimony.setTestimonyMovementList(movements);
        testimony.setCopyList(java.util.Collections.nCopies(copies, new Copy(1)));
        return testimony;
    }

    private DtoCaseTestimony onlyTestimony(DtoManagementCaseSummary summary) {
        assertThat(summary.deeds()).singleElement();
        DtoCaseDeed deed = summary.deeds().get(0);
        assertThat(deed.testimonies()).singleElement();
        return deed.testimonies().get(0);
    }

    private void givenProcedures(Procedure... procedures) {
        when(managementRepository.findById(4)).thenReturn(Optional.of(management()));
        when(procedureRepository.findByFkIdManagementIdManagement(4)).thenReturn(List.of(procedures));
    }

    @Test
    @DisplayName("Should list the deed, its testimony in state INSCRIPTO and its copies")
    void shouldListDeedTestimonyStateAndCopies() {
        givenProcedures(procedureWithTestimony(testimony(List.of(movement(1, null, true)), 1)));

        DtoManagementCaseSummary summary = service.getSummary(4);

        assertThat(summary.managementNumber()).isEqualTo(1001);
        assertThat(summary.deeds().get(0).idDeed()).isEqualTo(9);
        DtoCaseTestimony testimony = onlyTestimony(summary);
        assertThat(testimony.state()).isEqualTo("INSCRIPTO");
        assertThat(testimony.verified()).isTrue();
        assertThat(testimony.copies()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should report SIN_INGRESAR for a testimony without movements")
    void shouldReportSinIngresarWithoutMovements() {
        givenProcedures(procedureWithTestimony(testimony(List.of(), 0)));

        assertThat(onlyTestimony(service.getSummary(4)).state()).isEqualTo("SIN_INGRESAR");
    }

    @Test
    @DisplayName("Should report INGRESADO for an entered testimony not yet registered")
    void shouldReportIngresado() {
        givenProcedures(procedureWithTestimony(testimony(List.of(movement(1, null, false)), 0)));

        assertThat(onlyTestimony(service.getSummary(4)).state()).isEqualTo("INGRESADO");
    }

    @Test
    @DisplayName("Should report RETIRADO from the latest movement even when an earlier one was registered")
    void shouldReportRetiradoFromTheLatestMovement() {
        givenProcedures(procedureWithTestimony(
                testimony(List.of(movement(2, new Date(), true), movement(1, null, true)), 0)));

        assertThat(onlyTestimony(service.getSummary(4)).state()).isEqualTo("RETIRADO");
    }

    @Test
    @DisplayName("Should list a deed shared by several trámites once")
    void shouldListSharedDeedOnce() {
        Procedure first = procedureWithTestimony(testimony(List.of(), 0));
        Procedure second = new Procedure(2);
        second.setFkIdDeed(first.getFkIdDeed());
        givenProcedures(first, second);

        assertThat(service.getSummary(4).deeds()).hasSize(1);
    }

    @Test
    @DisplayName("Should return no deeds when no trámite has one")
    void shouldReturnEmptyDeedsWhenNoneExists() {
        givenProcedures(new Procedure(1));

        assertThat(service.getSummary(4).deeds()).isEmpty();
    }

    @Test
    @DisplayName("Should list the documents of the gestión with their status flags")
    void shouldListDocumentsOfTheManagement() {
        givenProcedures(new Procedure(7));
        SubmittedDocument document = new SubmittedDocument();
        document.setIdSubmittedDocument(31);
        document.setName("Certificado de dominio");
        document.setPrepared(true);
        document.setReleased(false);
        document.setFlagged(true);
        document.setDelivered(false);
        document.setReentered(true);
        document.setFkIdProcedure(new Procedure(7));
        when(submittedDocumentRepository.findByFkIdProcedureFkIdManagementIdManagement(4))
                .thenReturn(List.of(document));

        DtoManagementCaseSummary summary = service.getSummary(4);

        assertThat(summary.documents()).singleElement().satisfies((DtoCaseDocument row) -> {
            assertThat(row.idSubmittedDocument()).isEqualTo(31);
            assertThat(row.name()).isEqualTo("Certificado de dominio");
            assertThat(row.idProcedure()).isEqualTo(7);
            assertThat(row.prepared()).isTrue();
            assertThat(row.released()).isFalse();
            assertThat(row.observed()).isTrue();
            assertThat(row.reentered()).isTrue();
        });
    }

    @Test
    @DisplayName("Should return no documents when the gestión has none")
    void shouldReturnEmptyDocumentsWhenNoneExists() {
        givenProcedures(new Procedure(1));

        assertThat(service.getSummary(4).documents()).isEmpty();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for an unknown gestión")
    void shouldRejectUnknownManagement() {
        when(managementRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSummary(404)).isInstanceOf(ResourceNotFoundException.class);
    }
}
