package com.licensis.notaire.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU83 - Valida y aplica transiciones de estado de una gestión contra el
 * {@link WorkflowDefinition} del tipo de trámite asociado.
 */
@Service
public class GestionTransitionService {

    private static final Logger log = LoggerFactory.getLogger(GestionTransitionService.class);

    private final GestionDeEscrituraRepository gestionRepository;
    private final EstadoDeGestionRepository estadoRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final GestionBitacoraService gestionBitacoraService;

    public GestionTransitionService(GestionDeEscrituraRepository gestionRepository,
            EstadoDeGestionRepository estadoRepository,
            WorkflowTransitionRepository workflowTransitionRepository,
            GestionBitacoraService gestionBitacoraService) {
        this.gestionRepository = gestionRepository;
        this.estadoRepository = estadoRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.gestionBitacoraService = gestionBitacoraService;
    }

    /**
     * Valida que exista una {@link WorkflowTransition} desde el estado actual de la
     * gestión hacia {@code estadoDestino} y, de ser así, la aplica.
     */
    @Transactional
    public GestionDeEscritura transicionar(Integer idGestion, String estadoDestino) {
        GestionDeEscritura gestion = gestionRepository.findById(idGestion)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idGestion));

        WorkflowDefinition workflowDefinition = resolveWorkflowDefinition(gestion);

        EstadoDeGestion estadoActual = gestion.getFkIdEstadoDeGestion();
        EstadoDeGestion destino = estadoRepository.findByNombre(estadoDestino)
                .orElseThrow(() -> new BusinessValidationException(
                        "Estado destino '" + estadoDestino + "' no está definido en el sistema"));

        validarTransicion(workflowDefinition, estadoActual, destino);

        gestion.setFkIdEstadoDeGestion(destino);
        GestionDeEscritura gestionTransicionada = gestionRepository.save(gestion);
        gestionBitacoraService.registrarEstado(gestionTransicionada, null);
        log.info("Gestión {} transicionada a estado '{}'", idGestion, destino.getNombre());
        return gestionTransicionada;
    }

    private WorkflowDefinition resolveWorkflowDefinition(GestionDeEscritura gestion) {
        List<Tramite> tramites = gestion.getTramiteList();
        if (tramites == null || tramites.isEmpty()) {
            throw new BusinessValidationException(
                    "La gestión " + gestion.getIdGestion() + " no tiene trámites asociados");
        }
        TipoDeTramite tipoTramite = tramites.get(0).getFkIdTipoTramite();
        WorkflowDefinition workflowDefinition = tipoTramite != null ? tipoTramite.getWorkflowDefinition() : null;
        if (workflowDefinition == null) {
            throw new BusinessValidationException(
                    "La gestión " + gestion.getIdGestion() + " no tiene un workflow definido");
        }
        return workflowDefinition;
    }

    private void validarTransicion(WorkflowDefinition workflowDefinition, EstadoDeGestion origen,
            EstadoDeGestion destino) {
        List<WorkflowTransition> transiciones =
                workflowTransitionRepository.findByWorkflowDefinitionId(workflowDefinition.getId());
        boolean esValida = transiciones.stream().anyMatch(transicion ->
                coincideEstado(transicion.getNodoOrigen(), origen) && coincideEstado(transicion.getNodoDestino(), destino));
        if (!esValida) {
            throw new BusinessValidationException(
                    "Transición de '" + nombreEstado(origen) + "' a '" + destino.getNombre() + "' no está permitida");
        }
    }

    private static boolean coincideEstado(WorkflowNode nodo, EstadoDeGestion estado) {
        return nodo != null && nodo.getEstadoDeGestion() != null && estado != null
                && nodo.getEstadoDeGestion().getIdEstadoGestion().equals(estado.getIdEstadoGestion());
    }

    private static String nombreEstado(EstadoDeGestion estado) {
        return estado != null ? estado.getNombre() : "sin estado";
    }
}
