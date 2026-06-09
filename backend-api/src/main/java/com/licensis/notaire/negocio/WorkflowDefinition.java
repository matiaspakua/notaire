package com.licensis.notaire.negocio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_definition")
public class WorkflowDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_workflow_definition")
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo = false;

    @Version
    @Column(name = "version")
    private int version;

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WorkflowNode> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WorkflowTransition> transitions = new ArrayList<>();

    public WorkflowDefinition() {
    }

    public WorkflowDefinition(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<WorkflowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WorkflowNode> nodes) {
        this.nodes = nodes;
    }

    public List<WorkflowTransition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<WorkflowTransition> transitions) {
        this.transitions = transitions;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WorkflowDefinition other)) {
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
        return "WorkflowDefinition[id=" + id + ", nombre=" + nombre + "]";
    }
}
