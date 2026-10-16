/**
 * Document-reentry domain model (#1841 / ADR-030).
 */

export interface RequiredDocumentType {
  idDocumentType: number;
  name?: string;
  expires?: boolean;
  dueDays?: number;
  deliveredBy?: string;
}

export interface ProcedureRequiredDocumentation {
  idProcedure: number;
  typeProcedureName?: string;
  requiredDocuments: RequiredDocumentType[];
}

export interface ManagementDocumentReentry {
  idManagement: number;
  number?: number;
  heading?: string;
  procedures: ProcedureRequiredDocumentation[];
}

export interface DocumentReentryInput {
  idProcedure: number;
  idDocumentType: number;
}

export interface ReenteredDocument {
  idSubmittedDocument: number;
  idProcedure: number;
  idDocumentType: number;
  name?: string;
  expires: boolean;
  dueDays?: number;
  deliveredBy?: string;
  reentered: boolean;
}
