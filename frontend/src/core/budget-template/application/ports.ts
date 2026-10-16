import type { BudgetTemplate, BudgetTemplateWriteInput } from "../domain/budget-template";

export interface BudgetTemplateGateway {
  list(): Promise<BudgetTemplate[]>;
  create(input: BudgetTemplateWriteInput): Promise<void>;
  update(input: BudgetTemplateWriteInput): Promise<void>;
  delete(procedureTypeId: number, conceptId: number): Promise<void>;
}
