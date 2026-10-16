/**
 * HTTP adapter for document-cost templates (#1788 / ADR-030).
 */
import { apiGet, apiPost } from "@/lib/api-client";
import type {
  DocumentCostTemplate,
  DocumentCostTemplateCreateInput,
} from "@/core/document-cost-template/domain/document-cost-template";
import type { DocumentCostTemplateGateway } from "@/core/document-cost-template/application/ports";

interface WireRow {
  documentCostTemplatePK?: { fkIdProcedureType?: number; fkIdDocumentType?: number };
  fixedAmount?: number | null;
  variablePercentage?: number | null;
  version?: number;
}

function toDomain(row: WireRow): DocumentCostTemplate {
  return {
    procedureTypeId: row.documentCostTemplatePK?.fkIdProcedureType ?? 0,
    documentTypeId: row.documentCostTemplatePK?.fkIdDocumentType ?? 0,
    fixedAmount: row.fixedAmount,
    variablePercentage: row.variablePercentage,
    version: row.version,
  };
}

export const documentCostTemplateHttpGateway: DocumentCostTemplateGateway = {
  listByProcedureType: async (procedureTypeId: number) =>
    (
      await apiGet<WireRow[]>(`/document-cost-templates/procedure-types/${procedureTypeId}`)
    ).map(toDomain),
  create: async (input: DocumentCostTemplateCreateInput) => {
    await apiPost("/document-cost-templates", {
      idProcedureType: input.procedureTypeId,
      idDocumentType: input.documentTypeId,
      fixedAmount: input.fixedAmount,
      variablePercentage: input.variablePercentage,
    });
  },
};
