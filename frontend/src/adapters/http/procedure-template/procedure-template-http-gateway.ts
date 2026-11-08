/**
 * HTTP adapter for procedure templates (#1788 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { ProcedureTemplate } from "@/core/procedure-template/domain/procedure-template";
import type { ProcedureTemplateGateway } from "@/core/procedure-template/application/ports";

interface WireRow {
  fkIdProcedureType?: number;
  fkIdDocumentType?: number;
  notes?: string | null;
  version?: number;
}

function toDomain(row: WireRow): ProcedureTemplate {
  return {
    procedureTypeId: row.fkIdProcedureType ?? 0,
    documentTypeId: row.fkIdDocumentType ?? 0,
    notes: row.notes,
    version: row.version,
  };
}

export const procedureTemplateHttpGateway: ProcedureTemplateGateway = {
  listAll: async () => (await apiGet<WireRow[]>("/procedure-templates")).map(toDomain),
  listByProcedureType: async (procedureTypeId: number) =>
    (
      await apiGet<WireRow[]>(`/procedure-templates/procedure-types/${procedureTypeId}`)
    ).map(toDomain),
  create: async ({ procedureTypeId, documentTypeId, notes }) =>
    toDomain(
      await apiPost<WireRow>("/procedure-templates", {
        fkIdProcedureType: procedureTypeId,
        fkIdDocumentType: documentTypeId,
        notes: notes ?? "",
      }),
    ),
  update: async ({ procedureTypeId, documentTypeId, notes }) =>
    toDomain(
      await apiPut<WireRow>(`/procedure-templates/${procedureTypeId}/${documentTypeId}`, {
        notes,
      }),
    ),
  delete: async (procedureTypeId, documentTypeId) => {
    await apiDelete(`/procedure-templates/${procedureTypeId}/${documentTypeId}`);
  },
};
