/**
 * HTTP adapter for budgets (#1827 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiGetPage, apiPost, apiPut, ApiError } from "@/lib/api-client";
import type { Budget, BudgetItem, BudgetSummary } from "@/core/budget/domain/budget";
import type { BudgetGateway, BudgetInput, BudgetPage, BudgetPageRequest } from "@/core/budget/application/ports";

/** Wire shape from BudgetResponse / related DTOs — kept loose for OpenAPI drift. */
type BudgetDto = Budget;
type BudgetSummaryDto = BudgetSummary;
type BudgetItemDto = BudgetItem;

function toDomain(dto: BudgetDto): Budget {
  return {
    idBudget: dto.idBudget,
    number: dto.number,
    heading: dto.heading,
    status: dto.status,
    propertyAmount: dto.propertyAmount,
    notes: dto.notes,
    date: dto.date,
    person: dto.person
      ? {
          personId: dto.person.personId,
          name: dto.person.name,
          lastName: dto.person.lastName,
        }
      : undefined,
    version: dto.version,
  };
}

function toWire(input: BudgetInput): BudgetDto {
  return {
    idBudget: input.idBudget,
    number: input.number,
    heading: input.heading,
    status: input.status,
    propertyAmount: input.propertyAmount,
    notes: input.notes,
    date: input.date,
    person: input.person,
    version: input.version,
  };
}

function toItem(dto: BudgetItemDto): BudgetItem {
  return {
    idItem: dto.idItem,
    name: dto.name,
    value: dto.value,
    percentage: dto.percentage,
    notes: dto.notes,
    type: dto.type,
    reason: dto.reason,
    fixedConcept: dto.fixedConcept,
    budgetId: dto.budgetId,
    version: dto.version,
  };
}

export const budgetHttpGateway: BudgetGateway = {
  getById: async (id: number) => toDomain(await apiGet<BudgetDto>(`/budgets/${id}`)),
  findById: async (id: number) => {
    try {
      return toDomain(await apiGet<BudgetDto>(`/budgets/${id}`));
    } catch (e) {
      if (e instanceof ApiError && e.status === 404) return null;
      throw e;
    }
  },
  listPage: async (request: BudgetPageRequest): Promise<BudgetPage> => {
    const page = await apiGetPage<BudgetDto>("/budgets", {
      page: request.page,
      size: request.size,
      sort: request.sort,
    });
    return {
      content: page.content.map(toDomain),
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
    };
  },
  listByPerson: async (personId: number) =>
    (await apiGet<BudgetDto[]>(`/budgets/person/${personId}`)).map(toDomain),
  listByStatus: async (status: string) =>
    (await apiGet<BudgetDto[]>(`/budgets/search?status=${encodeURIComponent(status)}`)).map(toDomain),
  getSummary: async (id: number) => {
    const dto = await apiGet<BudgetSummaryDto>(`/budgets/${id}/summary`);
    return {
      idBudget: dto.idBudget,
      idManagement: dto.idManagement,
      managementHeading: dto.managementHeading,
      numberBudget: dto.numberBudget,
      numberManagement: dto.numberManagement,
      pendingBalance: dto.pendingBalance,
      total: dto.total,
      payments: dto.payments,
    };
  },
  create: async (input: BudgetInput) => toDomain(await apiPost<BudgetDto>("/budgets", toWire(input))),
  update: async (id: number, input: BudgetInput) => {
    await apiPut<void>(`/budgets/${id}`, toWire(input));
  },
  delete: async (id: number) => {
    await apiDelete(`/budgets/${id}`);
  },
  loadItemsFromTemplate: async (idBudget: number, procedureTypeId: number) =>
    (await apiPost<BudgetItemDto[]>(
      `/budgets/${idBudget}/items-from-template?procedureTypeId=${procedureTypeId}`,
      undefined,
    )).map(toItem),
  addItemsFromCatalog: async (idBudget: number, idItems: number[]) =>
    (await apiPost<BudgetItemDto[]>(`/budgets/${idBudget}/items-from-catalog`, { idItems })).map(toItem),
};
