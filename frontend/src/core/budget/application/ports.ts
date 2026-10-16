/**
 * Outbound port for budgets (#1827 / ADR-030).
 */
import type { Budget, BudgetItem, BudgetSummary } from "../domain/budget";

export interface BudgetPage {
  content: Budget[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface BudgetPageRequest {
  page: number;
  size: number;
  sort?: string;
}

export type BudgetInput = Partial<Budget>;

export interface BudgetGateway {
  getById(id: number): Promise<Budget>;
  /** Returns null when the budget does not exist (404). */
  findById(id: number): Promise<Budget | null>;
  listPage(request: BudgetPageRequest): Promise<BudgetPage>;
  listByPerson(personId: number): Promise<Budget[]>;
  listByStatus(status: string): Promise<Budget[]>;
  getSummary(id: number): Promise<BudgetSummary>;
  create(input: BudgetInput): Promise<Budget>;
  update(id: number, input: BudgetInput): Promise<void>;
  delete(id: number): Promise<void>;
  loadItemsFromTemplate(idBudget: number, procedureTypeId: number): Promise<BudgetItem[]>;
  addItemsFromCatalog(idBudget: number, idItems: number[]): Promise<BudgetItem[]>;
}
