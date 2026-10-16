/**
 * Document-cost-template domain (#1788 / ADR-030).
 */

export interface DocumentCostTemplate {
  procedureTypeId: number;
  documentTypeId: number;
  fixedAmount?: number | null;
  variablePercentage?: number | null;
  version?: number;
}

export interface DocumentCostTemplateCreateInput {
  procedureTypeId: number;
  documentTypeId: number;
  fixedAmount?: number | null;
  variablePercentage?: number | null;
}
