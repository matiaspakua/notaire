/**
 * HTTP adapter for budget templates (#1788 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { BudgetTemplate, BudgetTemplateWriteInput } from "@/core/budget-template/domain/budget-template";
import type { BudgetTemplateGateway } from "@/core/budget-template/application/ports";

interface WireRow {
  budgetTemplatePK?: { fkIdProcedureType?: number; fkIdConcept?: number };
  procedureType?: { idProcedureType?: number; name?: string };
  concept?: { idConcept?: number; name?: string };
  notes?: string;
  version?: number;
}

function toDomain(row: WireRow): BudgetTemplate {
  const procedureTypeId =
    row.budgetTemplatePK?.fkIdProcedureType ?? row.procedureType?.idProcedureType ?? 0;
  const conceptId = row.budgetTemplatePK?.fkIdConcept ?? row.concept?.idConcept ?? 0;
  return {
    procedureTypeId,
    conceptId,
    procedureTypeName: row.procedureType?.name,
    conceptName: row.concept?.name,
    notes: row.notes,
    version: row.version,
  };
}

function toWire(input: BudgetTemplateWriteInput): WireRow {
  return {
    budgetTemplatePK: {
      fkIdProcedureType: input.procedureTypeId,
      fkIdConcept: input.conceptId,
    },
    procedureType: { idProcedureType: input.procedureTypeId },
    concept: { idConcept: input.conceptId },
    notes: input.notes,
  };
}

export const budgetTemplateHttpGateway: BudgetTemplateGateway = {
  list: async () => (await apiGet<WireRow[]>("/budget-templates")).map(toDomain),
  create: async (input) => {
    await apiPost("/budget-templates", toWire(input));
  },
  update: async (input) => {
    await apiPut(
      `/budget-templates/procedure-types/${input.procedureTypeId}/concept/${input.conceptId}`,
      toWire(input),
    );
  },
  delete: async (procedureTypeId, conceptId) => {
    await apiDelete(`/budget-templates/procedure-types/${procedureTypeId}/concept/${conceptId}`);
  },
};
