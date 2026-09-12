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
public class DtoManagementWorkflowTrace {

    private Integer managementId;
    private Integer number;
    private String encabezado;
    private Date dateStart;
    private String statusActual;

    private DtoWorkflowDefinition workflowDefinition;
    private List<DtoWorkflowNode> nodes;
    private List<DtoWorkflowTransition> transitions;
    private List<DtoHistoryEntry> history;
    private Map<Integer, String> nodeStatuses; // nodeId → "completed" | "in_progress" | "pending"

    public Integer getManagementId() {
        return managementId;
    }

    public void setManagementId(Integer managementId) {
        this.managementId = managementId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public String getStatusActual() {
        return statusActual;
    }

    public void setStatusActual(String statusActual) {
        this.statusActual = statusActual;
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

    public List<DtoHistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<DtoHistoryEntry> history) {
        this.history = history;
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
    public static class DtoHistoryEntry {
        private Integer idHistory;
        private Integer statusManagementId;
        private String statusManagementName;
        private Date date;
        private String notes;

        public Integer getIdHistory() {
            return idHistory;
        }

        public void setIdHistory(Integer idHistory) {
            this.idHistory = idHistory;
        }

        public Integer getStatusManagementId() {
            return statusManagementId;
        }

        public void setStatusManagementId(Integer statusManagementId) {
            this.statusManagementId = statusManagementId;
        }

        public String getStatusManagementName() {
            return statusManagementName;
        }

        public void setStatusManagementName(String statusManagementName) {
            this.statusManagementName = statusManagementName;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
