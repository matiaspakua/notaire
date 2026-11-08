/**
 * Management domain model (#1839 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 * Field names align with REST wire shapes to limit mapping churn.
 */

export interface Management {
  idManagement?: number;
  number?: number;
  heading?: string;
  notes?: string;
  currentStatus?: string;
  dateStart?: string;
  procedureCount?: number;
}

export interface ManagementCompleteCase {
  idManagement?: number;
  number?: number;
  heading?: string;
  notes?: string;
  budgetId?: number;
  notaryId?: number;
  statusManagementId?: number;
  /** Domain name; gateway maps to wire `typeProcedureId`. */
  typeProcedureId?: number;
  propertyId?: number;
}

/** Input for POST/PUT complete-case (UI uses procedureTypeId). */
export interface CreateCompleteManagementInput {
  number: number;
  heading?: string;
  notes?: string;
  budgetId: number;
  notaryId: number;
  statusManagementId: number;
  procedureTypeId: number;
  propertyId?: number;
}

/** Loose create body for POST /managements. */
export type ManagementInput = Partial<Management>;

export interface OutstandingBalance {
  pendingBalance?: number;
}

export interface ArchivedManagement {
  idManagement: number;
  pendingBalance: number;
  pendingDebtAtArchiving: boolean;
}

export interface ManagementHistory {
  idHistory?: number;
  date?: string;
  notes?: string;
  managementId?: number;
  statusManagementId?: number;
  statusManagementName?: string;
}

export interface ProcedureFolder {
  idFolder?: number;
  number?: number;
  status?: string;
  waitReason?: string;
  idManagement?: number;
  idProcedure?: number;
}

export interface ManagementCaseSummary {
  managementId: number;
  managementNumber: number;
  heading?: string;
  deeds: unknown[];
  documents: unknown[];
}

export interface ManagementFinancialSummary {
  idManagement: number;
  totalBudgeted: number;
  totalCollected: number;
  pendingBalance: number;
}

export interface ManagementPage {
  content: Management[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ManagementPageRequest {
  page: number;
  size: number;
  sort?: string;
}
