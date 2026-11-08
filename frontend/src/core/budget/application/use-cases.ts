/**
 * Budget list/search/mutation use cases (#1827 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { BudgetGateway, BudgetInput, BudgetPageRequest } from "./ports";

export function createBudgetUseCases(gateway: BudgetGateway) {
  return {
    getById: (id: number) => gateway.getById(id),
    findById: (id: number) => gateway.findById(id),
    listPage: (request: BudgetPageRequest) => gateway.listPage(request),
    listByPerson: (personId: number) => gateway.listByPerson(personId),
    listByStatus: (status: string) => gateway.listByStatus(status),
    getSummary: (id: number) => gateway.getSummary(id),
    create: (input: BudgetInput) => gateway.create(input),
    update: (id: number, input: BudgetInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    loadItemsFromTemplate: (idBudget: number, procedureTypeId: number) =>
      gateway.loadItemsFromTemplate(idBudget, procedureTypeId),
    addItemsFromCatalog: (idBudget: number, idItems: number[]) =>
      gateway.addItemsFromCatalog(idBudget, idItems),
  };
}

export type BudgetUseCases = ReturnType<typeof createBudgetUseCases>;
