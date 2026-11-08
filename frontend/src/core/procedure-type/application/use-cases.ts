/**
 * Procedure-type catalog use cases (#1796 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { ProcedureTypeGateway, ProcedureTypeInput } from "./ports";

export function createProcedureTypeUseCases(gateway: ProcedureTypeGateway) {
  return {
    list: () => gateway.list(),
    searchByName: (name: string) => gateway.searchByName(name),
    create: (input: ProcedureTypeInput) => gateway.create(input),
    update: (id: number, input: ProcedureTypeInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    isInUse: (id: number) => gateway.isInUse(id),
    assignWorkflow: (id: number, workflowDefinitionId: number | null) =>
      gateway.assignWorkflow(id, workflowDefinitionId),
  };
}

export type ProcedureTypeUseCases = ReturnType<typeof createProcedureTypeUseCases>;
