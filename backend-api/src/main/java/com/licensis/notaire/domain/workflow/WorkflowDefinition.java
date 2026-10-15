package com.licensis.notaire.domain.workflow;

/**
 * A workflow definition (status workflow catalog entry). Plain Java (issue #1728, ADR-030).
 */
public record WorkflowDefinition(Integer id, String name, String description, boolean active, int version) {

    public static WorkflowDefinition create(String name, String description, boolean active) {
        return new WorkflowDefinition(null, name, description, active, 0);
    }

    public static WorkflowDefinition restore(
            Integer id, String name, String description, boolean active, int version) {
        return new WorkflowDefinition(id, name, description, active, version);
    }

    public WorkflowDefinition changeTo(String newName, String newDescription, boolean newActive, Integer newVersion) {
        int versionToKeep = newVersion != null ? newVersion : version;
        return new WorkflowDefinition(id, newName, newDescription, newActive, versionToKeep);
    }

    public WorkflowDefinition withIdentity(Integer newId, int newVersion) {
        return new WorkflowDefinition(newId, name, description, active, newVersion);
    }
}
