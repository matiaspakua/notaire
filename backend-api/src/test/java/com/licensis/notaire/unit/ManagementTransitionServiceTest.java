package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.service.ManagementBitacoraService;
import com.licensis.notaire.service.ManagementTransitionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU83"})
@DisplayName("GestionTransitionService Tests")
@ExtendWith(MockitoExtension.class)
class ManagementTransitionServiceTest {

    @Mock
    private DeedManagementRepository managementRepository;

    @Mock
    private ManagementStatusRepository statusRepository;

    @Mock
    private WorkflowTransitionRepository workflowTransitionRepository;

    @Mock
    private ManagementBitacoraService managementBitacoraService;

    @InjectMocks
    private ManagementTransitionService managementTransitionService;

    private DeedManagement management;
    private ManagementStatus statusInicial;
    private ManagementStatus statusDestination;
    private WorkflowDefinition workflowDefinition;
    private WorkflowNode originNode;
    private WorkflowNode destinationNode;

    @BeforeEach
    void setUp() {
        statusInicial = new ManagementStatus(1, "Iniciada");
        statusDestination = new ManagementStatus(2, "En trámite");

        workflowDefinition = new WorkflowDefinition(1);

        ProcedureType typeProcedure = new ProcedureType();
        typeProcedure.setWorkflowDefinition(workflowDefinition);

        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(typeProcedure);

        management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(statusInicial);
        management.setProcedureList(List.of(procedure));

        originNode = new WorkflowNode(1);
        originNode.setManagementStatus(statusInicial);

        destinationNode = new WorkflowNode(2);
        destinationNode.setManagementStatus(statusDestination);
    }

    @Test
    @DisplayName("Should apply valid transition")
    void shouldApplyValidTransition() {
        WorkflowTransition transicion = new WorkflowTransition(1);
        transicion.setOriginNode(originNode);
        transicion.setDestinationNode(destinationNode);

        when(managementRepository.findById(1)).thenReturn(Optional.of(management));
        when(statusRepository.findByName("En trámite")).thenReturn(Optional.of(statusDestination));
        when(workflowTransitionRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of(transicion));
        when(managementRepository.save(management)).thenReturn(management);

        DeedManagement result = managementTransitionService.transicionar(1, "En trámite");

        assertThat(result.getFkIdManagementStatus()).isEqualTo(statusDestination);
    }

    @Test
    @DisplayName("Should reject invalid transition")
    void shouldRejectInvalidTransition() {
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));
        when(statusRepository.findByName("En trámite")).thenReturn(Optional.of(statusDestination));
        when(workflowTransitionRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of());

        assertThatThrownBy(() -> managementTransitionService.transicionar(1, "En trámite"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Iniciada")
                .hasMessageContaining("En trámite");
    }

    @Test
    @DisplayName("Should reject transition when gestión has no workflow definition")
    void shouldRejectTransitionWhenNoWorkflowDefinition() {
        Procedure procedureSinType = new Procedure();
        management.setProcedureList(List.of(procedureSinType));

        when(managementRepository.findById(1)).thenReturn(Optional.of(management));

        assertThatThrownBy(() -> managementTransitionService.transicionar(1, "En trámite"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("workflow");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when gestión does not exist")
    void shouldRejectTransitionWhenManagementNotFound() {
        when(managementRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> managementTransitionService.transicionar(999, "En trámite"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
