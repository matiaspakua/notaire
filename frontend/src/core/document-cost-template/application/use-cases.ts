import type { DocumentCostTemplateGateway } from "./ports";
import type { DocumentCostTemplateCreateInput } from "../domain/document-cost-template";

export function createDocumentCostTemplateUseCases(gateway: DocumentCostTemplateGateway) {
  return {
    listByProcedureType: (id: number) => gateway.listByProcedureType(id),
    create: (input: DocumentCostTemplateCreateInput) => gateway.create(input),
  };
}

export type DocumentCostTemplateUseCases = ReturnType<typeof createDocumentCostTemplateUseCases>;
