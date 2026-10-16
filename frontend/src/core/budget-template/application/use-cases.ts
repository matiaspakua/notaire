import type { BudgetTemplateGateway } from "./ports";
import type { BudgetTemplateWriteInput } from "../domain/budget-template";

export function createBudgetTemplateUseCases(gateway: BudgetTemplateGateway) {
  return {
    list: () => gateway.list(),
    create: (input: BudgetTemplateWriteInput) => gateway.create(input),
    update: (input: BudgetTemplateWriteInput) => gateway.update(input),
    delete: (procedureTypeId: number, conceptId: number) =>
      gateway.delete(procedureTypeId, conceptId),
  };
}

export type BudgetTemplateUseCases = ReturnType<typeof createBudgetTemplateUseCases>;
