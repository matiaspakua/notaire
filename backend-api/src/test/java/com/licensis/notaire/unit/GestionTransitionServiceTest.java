package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.service.GestionBitacoraService;
import com.licensis.notaire.service.GestionTransitionService;
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
class GestionTransitionServiceTest {

    @Mock
    private GestionDeEscrituraRepository gestionRepository;

    @Mock
    private EstadoDeGestionRepository estadoRepository;

    @Mock
    private WorkflowTransitionRepository workflowTransitionRepository;

    @Mock
    private GestionBitacoraService gestionBitacoraService;

    @InjectMocks
    private GestionTransitionService gestionTransitionService;

    private GestionDeEscritura gestion;
    private EstadoDeGestion estadoInicial;
    private EstadoDeGestion estadoDestino;
    private WorkflowDefinition workflowDefinition;
    private WorkflowNode nodoOrigen;
    private WorkflowNode nodoDestino;

    @BeforeEach
    void setUp() {
        estadoInicial = new EstadoDeGestion(1, "Iniciada");
        estadoDestino = new EstadoDeGestion(2, "En trámite");

        workflowDefinition = new WorkflowDefinition(1);

        TipoDeTramite tipoTramite = new TipoDeTramite();
        tipoTramite.setWorkflowDefinition(workflowDefinition);

        Tramite tramite = new Tramite();
        tramite.setFkIdTipoTramite(tipoTramite);

        gestion = new GestionDeEscritura();
        gestion.setIdGestion(1);
        gestion.setFkIdEstadoDeGestion(estadoInicial);
        gestion.setTramiteList(List.of(tramite));

        nodoOrigen = new WorkflowNode(1);
        nodoOrigen.setEstadoDeGestion(estadoInicial);

        nodoDestino = new WorkflowNode(2);
        nodoDestino.setEstadoDeGestion(estadoDestino);
    }

    @Test
    @DisplayName("Should apply valid transition")
    void shouldApplyValidTransition() {
        WorkflowTransition transicion = new WorkflowTransition(1);
        transicion.setNodoOrigen(nodoOrigen);
        transicion.setNodoDestino(nodoDestino);

        when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
        when(estadoRepository.findByNombre("En trámite")).thenReturn(Optional.of(estadoDestino));
        when(workflowTransitionRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of(transicion));
        when(gestionRepository.save(gestion)).thenReturn(gestion);

        GestionDeEscritura result = gestionTransitionService.transicionar(1, "En trámite");

        assertThat(result.getFkIdEstadoDeGestion()).isEqualTo(estadoDestino);
    }

    @Test
    @DisplayName("Should reject invalid transition")
    void shouldRejectInvalidTransition() {
        when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
        when(estadoRepository.findByNombre("En trámite")).thenReturn(Optional.of(estadoDestino));
        when(workflowTransitionRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of());

        assertThatThrownBy(() -> gestionTransitionService.transicionar(1, "En trámite"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Iniciada")
                .hasMessageContaining("En trámite");
    }

    @Test
    @DisplayName("Should reject transition when gestión has no workflow definition")
    void shouldRejectTransitionWhenNoWorkflowDefinition() {
        Tramite tramiteSinTipo = new Tramite();
        gestion.setTramiteList(List.of(tramiteSinTipo));

        when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));

        assertThatThrownBy(() -> gestionTransitionService.transicionar(1, "En trámite"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("workflow");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when gestión does not exist")
    void shouldRejectTransitionWhenGestionNotFound() {
        when(gestionRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestionTransitionService.transicionar(999, "En trámite"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
