// @vitest-environment node
/**
 * Core use cases for workflows admin (#1792 / ADR-030).
 * Fake gateway only — no React, Next, or api-client.
 */
import { describe, expect, it } from "vitest";
import type { WorkflowDefinition, WorkflowNode, WorkflowTransition } from "../domain/workflow";
import type { ManagementWorkflowGateway, WorkflowGateway } from "./ports";
import { createManagementWorkflowUseCases, createWorkflowUseCases } from "./use-cases";

function fakeWorkflowGateway(): WorkflowGateway & {
  definitions: WorkflowDefinition[];
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
} {
  const definitions: WorkflowDefinition[] = [{ id: 1, name: "Default", active: true }];
  const nodes: WorkflowNode[] = [{ id: 10, workflowDefinitionId: 1, type: "INITIAL" }];
  const transitions: WorkflowTransition[] = [];
  let nextDef = 2;
  let nextNode = 11;
  let nextTr = 1;
  return {
    definitions,
    nodes,
    transitions,
    listDefinitions: async () => [...definitions],
    createDefinition: async (data) => {
      const created = { id: nextDef++, ...data };
      definitions.push(created);
      return created;
    },
    updateDefinition: async (id, data) => {
      const idx = definitions.findIndex((d) => d.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      definitions[idx] = { ...definitions[idx], ...data };
    },
    deleteDefinition: async (id) => {
      const idx = definitions.findIndex((d) => d.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      definitions.splice(idx, 1);
    },
    validateDefinition: async (id) =>
      id === 1 ? { valid: true, errors: [] } : { valid: false, errors: ["no nodes"] },
    listNodesByWorkflow: async (workflowId) => nodes.filter((n) => n.workflowDefinitionId === workflowId),
    createNode: async (data) => {
      const created = { id: nextNode++, ...data };
      nodes.push(created);
      return created;
    },
    updateNode: async (id, data) => {
      const idx = nodes.findIndex((n) => n.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      nodes[idx] = { ...nodes[idx], ...data };
    },
    deleteNode: async (id) => {
      const idx = nodes.findIndex((n) => n.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      nodes.splice(idx, 1);
    },
    listTransitionsByWorkflow: async (workflowId) =>
      transitions.filter((t) => t.workflowDefinitionId === workflowId),
    createTransition: async (data) => {
      const created = { id: nextTr++, ...data };
      transitions.push(created);
      return created;
    },
    updateTransition: async (id, data) => {
      const idx = transitions.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      transitions[idx] = { ...transitions[idx], ...data };
      return transitions[idx];
    },
    deleteTransition: async (id) => {
      const idx = transitions.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      transitions.splice(idx, 1);
    },
  };
}

describe("workflow use cases (#1792)", () => {
  it("lists and mutates definitions via the gateway", async () => {
    const gateway = fakeWorkflowGateway();
    const uc = createWorkflowUseCases(gateway);

    await expect(uc.listDefinitions()).resolves.toEqual([{ id: 1, name: "Default", active: true }]);
    const created = await uc.createDefinition({ name: "Alt", active: false });
    expect(created.id).toBe(2);
    await uc.updateDefinition(2, { active: true });
    await expect(uc.validateDefinition(1)).resolves.toEqual({ valid: true, errors: [] });
    await uc.deleteDefinition(2);
    await expect(uc.listDefinitions()).resolves.toHaveLength(1);
  });

  it("lists and mutates nodes and transitions", async () => {
    const gateway = fakeWorkflowGateway();
    const uc = createWorkflowUseCases(gateway);

    await expect(uc.listNodesByWorkflow(1)).resolves.toHaveLength(1);
    const node = await uc.createNode({ workflowDefinitionId: 1, type: "FINAL" });
    await uc.updateNode(node.id!, { positionX: 40 });
    const tr = await uc.createTransition({
      workflowDefinitionId: 1,
      originNodeId: 10,
      destinationNodeId: node.id,
    });
    await uc.updateTransition(tr.id!, { description: "done" });
    await expect(uc.listTransitionsByWorkflow(1)).resolves.toHaveLength(1);
    await uc.deleteTransition(tr.id!);
    await uc.deleteNode(node.id!);
    await expect(uc.listNodesByWorkflow(1)).resolves.toHaveLength(1);
  });
});

describe("management workflow use cases (#1792)", () => {
  it("loads a trace and finds the latest traced management", async () => {
    const traces = new Map([
      [
        5,
        {
          managementId: 5,
          nodes: [],
          transitions: [],
          history: [],
          nodeStatuses: {},
        },
      ],
    ]);
    const gateway: ManagementWorkflowGateway = {
      getTrace: async (id) => {
        const t = traces.get(id);
        if (!t) throw new Error("no trace");
        return t;
      },
      listManagementIds: async () => ({ content: [{ idManagement: 9 }, { idManagement: 5 }] }),
      findLatestTraced: async (candidateLimit, batchSize, sort) => {
        const page = await gateway.listManagementIds(0, candidateLimit, sort);
        const ids = page.content.map((g) => g.idManagement).filter((id): id is number => id != null);
        for (let i = 0; i < ids.length; i += batchSize) {
          const batch = ids.slice(i, i + batchSize);
          for (const id of batch) {
            try {
              const trace = await gateway.getTrace(id);
              return { managementId: id, trace };
            } catch {
              /* try next */
            }
          }
        }
        return null;
      },
    };
    const uc = createManagementWorkflowUseCases(gateway);
    await expect(uc.getTrace(5)).resolves.toMatchObject({ managementId: 5 });
    await expect(uc.findLatestTraced(20, 5, "idManagement,desc")).resolves.toMatchObject({
      managementId: 5,
    });
  });
});
