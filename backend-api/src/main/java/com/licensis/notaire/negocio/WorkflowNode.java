package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoWorkflowNode;
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
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "workflow_node")
public class WorkflowNode implements Serializable, Persistable<Integer> {

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
    private ManagementStatus managementStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private WorkflowNodeType type;

    @Column(name = "posicion_x")
    private Float positionX;

    @Column(name = "posicion_y")
    private Float positionY;

    @Version
    @Column(name = "version")
    private int version;

    public WorkflowNode() {
    }

    public WorkflowNode(Integer id) {
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

    public ManagementStatus getManagementStatus() {
        return managementStatus;
    }

    public void setManagementStatus(ManagementStatus managementStatus) {
        this.managementStatus = managementStatus;
    }

    public WorkflowNodeType getType() {
        return type;
    }

    public void setType(WorkflowNodeType type) {
        this.type = type;
    }

    public Float getPositionX() {
        return positionX;
    }

    public void setPositionX(Float positionX) {
        this.positionX = positionX;
    }

    public Float getPositionY() {
        return positionY;
    }

    public void setPositionY(Float positionY) {
        this.positionY = positionY;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public DtoWorkflowNode toDto() {
        DtoWorkflowNode dto = new DtoWorkflowNode();
        dto.setId(this.id);
        dto.setWorkflowDefinitionId(workflowDefinition != null ? workflowDefinition.getId() : null);
        dto.setStatusManagementId(managementStatus != null ? managementStatus.getIdManagementStatus() : null);
        dto.setStatusManagementName(managementStatus != null ? managementStatus.getName() : null);
        dto.setType(type != null ? type.name() : null);
        dto.setPositionX(this.positionX);
        dto.setPositionY(this.positionY);
        dto.setVersion(this.version);
        return dto;
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
        return "WorkflowNode[id=" + id + ", tipo=" + type + "]";
    }
}
