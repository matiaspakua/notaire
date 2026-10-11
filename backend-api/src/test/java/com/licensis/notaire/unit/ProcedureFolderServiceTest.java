package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.application.port.out.procedure.ProcedureFolderRepositoryPort;
import com.licensis.notaire.application.usecase.procedure.ProcedureFolderService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU85"})
@DisplayName("ProcedureFolderService tests")
@ExtendWith(MockitoExtension.class)
class ProcedureFolderServiceTest {

    @Mock
    private ProcedureFolderRepositoryPort procedureFolderRepository;

    @InjectMocks
    private ProcedureFolderService procedureFolderService;

    private DeedManagement management;
    private Procedure procedure;

    @BeforeEach
    void setUp() {
        management = new DeedManagement();
        management.setIdManagement(1);
        procedure = new Procedure();
        procedure.setIdProcedure(10);
        procedure.setFkIdManagement(management);
    }

    @Test
    @DisplayName("Should generate an active numbered folder when a procedure is created")
    void shouldGenerateActiveFolderForNewProcedure() {
        when(procedureFolderRepository.nextFolderNumber()).thenReturn(1);
        when(procedureFolderRepository.save(any(ProcedureFolder.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcedureFolder folder = procedureFolderService.generateFolderForProcedure(procedure);

        assertThat(folder.getNumber()).isEqualTo(1);
        assertThat(folder.getStatus()).isEqualTo("Activa");
        assertThat(folder.getFkIdManagement()).isEqualTo(management);
        assertThat(folder.getFkIdProcedure()).isEqualTo(procedure);
    }

    @Test
    @DisplayName("Should number the folder from the database sequence, not from the last folder")
    void shouldNumberFolderFromSequence() {
        when(procedureFolderRepository.nextFolderNumber()).thenReturn(8);
        when(procedureFolderRepository.save(any(ProcedureFolder.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcedureFolder folder = procedureFolderService.generateFolderForProcedure(procedure);

        assertThat(folder.getNumber()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should put a folder on hold when a reason is provided")
    void shouldPutFolderOnHoldWithReason() {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(1);
        folder.setStatus("Activa");
        when(procedureFolderRepository.findById(1)).thenReturn(Optional.of(folder));
        when(procedureFolderRepository.save(any(ProcedureFolder.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcedureFolder result = procedureFolderService.putOnHold(1, "Missing documentation of the holder");

        assertThat(result.getStatus()).isEqualTo("Espera");
        assertThat(result.getWaitReason()).isEqualTo("Missing documentation of the holder");
        ArgumentCaptor<ProcedureFolder> captor = ArgumentCaptor.forClass(ProcedureFolder.class);
        verify(procedureFolderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("Espera");
    }

    @Test
    @DisplayName("Should reject putting a folder on hold without a reason (CU85 - exception 3.1)")
    void shouldRejectWaitWithoutReason() {
        assertThatThrownBy(() -> procedureFolderService.putOnHold(1, "  "))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("reason");
    }

    @Test
    @DisplayName("Should throw when folder does not exist")
    void shouldThrowWhenFolderNotFound() {
        when(procedureFolderRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procedureFolderService.putOnHold(999, "Valid reason"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find folders by management")
    void shouldFindFoldersByManagement() {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(1);
        when(procedureFolderRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(folder));

        List<ProcedureFolder> folders = procedureFolderService.findByManagement(1);

        assertThat(folders).containsExactly(folder);
    }

    @Test
    @DisplayName("Should find folder by procedure")
    void shouldFindFolderByProcedure() {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(1);
        when(procedureFolderRepository.findByFkIdProcedureIdProcedure(10)).thenReturn(Optional.of(folder));

        Optional<ProcedureFolder> result = procedureFolderService.findByProcedure(10);

        assertThat(result).contains(folder);
    }
}
