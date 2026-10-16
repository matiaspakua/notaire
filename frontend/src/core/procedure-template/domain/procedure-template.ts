/**
 * Procedure-template (required-documents checklist) domain (#1788 / ADR-030).
 */

export interface ProcedureTemplate {
  procedureTypeId: number;
  documentTypeId: number;
  notes?: string | null;
  version?: number;
}
