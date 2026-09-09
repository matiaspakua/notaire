package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoWorkflowDefinition;
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
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_definition")
public class WorkflowDefinition implements Serializable, Persistable<Integer> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_workflow_definition")
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "activo", nullable = false)
    private boolean active = false;

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
    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    public boolean isNew() {
        return id == null;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public DtoWorkflowDefinition toDto() {
        DtoWorkflowDefinition dto = new DtoWorkflowDefinition();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setDescription(this.description);
        dto.setActive(this.active);
        dto.setVersion(this.version);
        return dto;
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
        return "WorkflowDefinition[id=" + id + ", nombre=" + name + "]";
    }
}
