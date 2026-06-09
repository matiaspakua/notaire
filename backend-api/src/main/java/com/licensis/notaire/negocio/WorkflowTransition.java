package com.licensis.notaire.negocio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;

@Entity
@Table(name = "workflow_transition")
public class WorkflowTransition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_workflow_transition")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "fk_workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @ManyToOne
    @JoinColumn(name = "fk_nodo_origen_id", nullable = false)
    private WorkflowNode nodoOrigen;

    @ManyToOne
    @JoinColumn(name = "fk_nodo_destino_id", nullable = false)
    private WorkflowNode nodoDestino;

    @Column(name = "condicion")
    private String condicion;

    @Column(name = "descripcion")
    private String descripcion;

    @Version
    @Column(name = "version")
    private int version;

    public WorkflowTransition() {
    }

    public WorkflowTransition(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public WorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public WorkflowNode getNodoOrigen() {
        return nodoOrigen;
    }

    public void setNodoOrigen(WorkflowNode nodoOrigen) {
        this.nodoOrigen = nodoOrigen;
    }

    public WorkflowNode getNodoDestino() {
        return nodoDestino;
    }

    public void setNodoDestino(WorkflowNode nodoDestino) {
        this.nodoDestino = nodoDestino;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WorkflowTransition other)) {
            return false;
        }
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "WorkflowTransition[id=" + id + "]";
    }
}
