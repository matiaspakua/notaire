package com.licensis.notaire.negocio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;

@Entity
@Table(name = "workflow_node")
public class WorkflowNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_workflow_node")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "fk_workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @ManyToOne
    @JoinColumn(name = "fk_estado_gestion_id", nullable = false)
    private EstadoDeGestion estadoDeGestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private WorkflowNodeType tipo;

    @Column(name = "posicion_x")
    private Float posicionX;

    @Column(name = "posicion_y")
    private Float posicionY;

    @Version
    @Column(name = "version")
    private int version;

    public WorkflowNode() {
    }

    public WorkflowNode(Integer id) {
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

    public EstadoDeGestion getEstadoDeGestion() {
        return estadoDeGestion;
    }

    public void setEstadoDeGestion(EstadoDeGestion estadoDeGestion) {
        this.estadoDeGestion = estadoDeGestion;
    }

    public WorkflowNodeType getTipo() {
        return tipo;
    }

    public void setTipo(WorkflowNodeType tipo) {
        this.tipo = tipo;
    }

    public Float getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(Float posicionX) {
        this.posicionX = posicionX;
    }

    public Float getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(Float posicionY) {
        this.posicionY = posicionY;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WorkflowNode other)) {
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
        return "WorkflowNode[id=" + id + ", tipo=" + tipo + "]";
    }
}
