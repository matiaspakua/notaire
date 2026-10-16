/**
 * Budget-template domain for admin (#1788 / ADR-030).
 */

export interface BudgetTemplate {
  procedureTypeId: number;
  conceptId: number;
  procedureTypeName?: string;
  conceptName?: string;
  notes?: string;
  version?: number;
}

export interface BudgetTemplateWriteInput {
  procedureTypeId: number;
  conceptId: number;
  notes?: string;
}
