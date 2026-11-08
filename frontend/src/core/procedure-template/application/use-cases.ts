/**
 * Procedure-template use cases (#1788 / ADR-030).
 */
import type { ProcedureTemplateGateway } from "./ports";

export function createProcedureTemplateUseCases(gateway: ProcedureTemplateGateway) {
  return {
    listAll: () => gateway.listAll(),
    listByProcedureType: (id: number) => gateway.listByProcedureType(id),
    create: gateway.create.bind(gateway),
    update: gateway.update.bind(gateway),
    delete: (procedureTypeId: number, documentTypeId: number) =>
      gateway.delete(procedureTypeId, documentTypeId),
  };
}

export type ProcedureTemplateUseCases = ReturnType<typeof createProcedureTemplateUseCases>;
