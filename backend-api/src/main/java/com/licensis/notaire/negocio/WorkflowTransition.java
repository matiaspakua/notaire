package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoWorkflowTransition;
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
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "workflow_transition")
public class WorkflowTransition implements Serializable, Persistable<Integer> {

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
    private WorkflowNode originNode;

    @ManyToOne
    @JoinColumn(name = "fk_nodo_destino_id", nullable = false)
    private WorkflowNode destinationNode;

    @Column(name = "condicion")
    private String condition;

    @Column(name = "descripcion")
    private String description;

    @Version
    @Column(name = "version")
    private int version;

    public WorkflowTransition() {
    }

    public WorkflowTransition(Integer id) {
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

    public WorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public WorkflowNode getOriginNode() {
        return originNode;
    }

    public void setOriginNode(WorkflowNode originNode) {
        this.originNode = originNode;
    }

    public WorkflowNode getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(WorkflowNode destinationNode) {
        this.destinationNode = destinationNode;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public DtoWorkflowTransition toDto() {
        DtoWorkflowTransition dto = new DtoWorkflowTransition();
        dto.setId(this.id);
        dto.setWorkflowDefinitionId(workflowDefinition != null ? workflowDefinition.getId() : null);
        dto.setOriginNodeId(originNode != null ? originNode.getId() : null);
        dto.setDestinationNodeId(destinationNode != null ? destinationNode.getId() : null);
        dto.setCondition(this.condition);
        dto.setDescription(this.description);
        dto.setVersion(this.version);
        return dto;
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
