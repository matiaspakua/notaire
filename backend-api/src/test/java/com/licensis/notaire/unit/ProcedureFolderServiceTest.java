package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import com.licensis.notaire.service.ProcedureFolderService;
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
@DisplayName("CarpetaTramiteService Tests")
@ExtendWith(MockitoExtension.class)
class ProcedureFolderServiceTest {

    @Mock
    private ProcedureFolderRepository procedureFolderRepository;

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
    @DisplayName("Should generate carpeta activa numerada al alta de un trámite")
    void shouldGenerateActiveFolderForNewProcedure() {
        when(procedureFolderRepository.findTopByOrderByNumberDesc()).thenReturn(Optional.empty());
        when(procedureFolderRepository.save(any(ProcedureFolder.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcedureFolder folder = procedureFolderService.generarFolderParaProcedure(procedure);

        assertThat(folder.getNumber()).isEqualTo(1);
        assertThat(folder.getStatus()).isEqualTo("Activa");
        assertThat(folder.getFkIdManagement()).isEqualTo(management);
        assertThat(folder.getFkIdProcedure()).isEqualTo(procedure);
    }

    @Test
    @DisplayName("Should increment number based on the last generated carpeta")
    void shouldIncrementNumberFromLastFolder() {
        ProcedureFolder ultima = new ProcedureFolder();
        ultima.setNumber(7);
        when(procedureFolderRepository.findTopByOrderByNumberDesc()).thenReturn(Optional.of(ultima));
        when(procedureFolderRepository.save(any(ProcedureFolder.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcedureFolder folder = procedureFolderService.generarFolderParaProcedure(procedure);

        assertThat(folder.getNumber()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should put carpeta en espera when motivo is provided")
    void shouldPutFolderEnWaitWithReason() {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(1);
        folder.setStatus("Activa");
        when(procedureFolderRepository.findById(1)).thenReturn(Optional.of(folder));
        when(procedureFolderRepository.save(any(ProcedureFolder.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcedureFolder result = procedureFolderService.ponerEnWait(1, "Falta documentación del titular");

        assertThat(result.getStatus()).isEqualTo("Espera");
        assertThat(result.getWaitReason()).isEqualTo("Falta documentación del titular");
        ArgumentCaptor<ProcedureFolder> captor = ArgumentCaptor.forClass(ProcedureFolder.class);
        verify(procedureFolderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("Espera");
    }

    @Test
    @DisplayName("Should reject poner en espera without a motivo (CU85 - Excepción 3.1)")
    void shouldRejectWaitWithoutReason() {
        assertThatThrownBy(() -> procedureFolderService.ponerEnWait(1, "  "))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Should throw when carpeta does not exist")
    void shouldThrowWhenFolderNotFound() {
        when(procedureFolderRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procedureFolderService.ponerEnWait(999, "Motivo válido"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find carpetas by gestión")
    void shouldFindCarpetasByManagement() {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(1);
        when(procedureFolderRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(folder));

        List<ProcedureFolder> carpetas = procedureFolderService.findByManagement(1);

        assertThat(carpetas).containsExactly(folder);
    }

    @Test
    @DisplayName("Should find carpeta by trámite")
    void shouldFindFolderByProcedure() {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(1);
        when(procedureFolderRepository.findByFkIdProcedureIdProcedure(10)).thenReturn(Optional.of(folder));

        Optional<ProcedureFolder> result = procedureFolderService.findByProcedure(10);

        assertThat(result).contains(folder);
    }
}
