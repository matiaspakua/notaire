package com.licensis.notaire.dto;

import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.dto.DtoWorkflowTransition;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Aggregated DTO for the GET /api/v1/gestiones/{id}/workflow-trace endpoint.
 * Combines gestión info, the workflow definition, its nodes and transitions,
 * the gestión's historial, and computed per-node statuses.
 */
public class DtoGestionWorkflowTrace {

    private Integer gestionId;
    private Integer numero;
    private String encabezado;
    private Date fechaInicio;
    private String estadoActual;

    private DtoWorkflowDefinition workflowDefinition;
    private List<DtoWorkflowNode> nodes;
    private List<DtoWorkflowTransition> transitions;
    private List<DtoHistorialEntry> historial;
    private Map<Integer, String> nodeStatuses; // nodeId → "completed" | "in_progress" | "pending"

    public Integer getGestionId() {
        return gestionId;
    }

    public void setGestionId(Integer gestionId) {
        this.gestionId = gestionId;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public DtoWorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(DtoWorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public List<DtoWorkflowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<DtoWorkflowNode> nodes) {
        this.nodes = nodes;
    }

    public List<DtoWorkflowTransition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<DtoWorkflowTransition> transitions) {
        this.transitions = transitions;
    }

    public List<DtoHistorialEntry> getHistorial() {
        return historial;
    }

    public void setHistorial(List<DtoHistorialEntry> historial) {
        this.historial = historial;
    }

    public Map<Integer, String> getNodeStatuses() {
        return nodeStatuses;
    }

    public void setNodeStatuses(Map<Integer, String> nodeStatuses) {
        this.nodeStatuses = nodeStatuses;
    }

    /**
     * Lightweight historial entry included in the workflow trace response.
     */
    public static class DtoHistorialEntry {
        private Integer idHistorial;
        private Integer estadoGestionId;
        private String estadoGestionNombre;
        private Date fecha;
        private String observaciones;

        public Integer getIdHistorial() {
            return idHistorial;
        }

        public void setIdHistorial(Integer idHistorial) {
            this.idHistorial = idHistorial;
        }

        public Integer getEstadoGestionId() {
            return estadoGestionId;
        }

        public void setEstadoGestionId(Integer estadoGestionId) {
            this.estadoGestionId = estadoGestionId;
        }

        public String getEstadoGestionNombre() {
            return estadoGestionNombre;
        }

        public void setEstadoGestionNombre(String estadoGestionNombre) {
            this.estadoGestionNombre = estadoGestionNombre;
        }

        public Date getFecha() {
            return fecha;
        }

        public void setFecha(Date fecha) {
            this.fecha = fecha;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }
    }
}
