/**
 * React Query hooks for ProcedureTemplate: the required-documents checklist per procedure type
 * (CU03 read, CU55 admin #1362).
 * Endpoints: GET /api/v1/procedure-templates/procedure-types/{idProcedureType},
 * POST /api/v1/procedure-templates, PUT/DELETE /api/v1/procedure-templates/{idProcedureType}/{idDocumentType}
 *
 * The API answers flat rows (fkIdProcedureType, fkIdDocumentType, notes), so the document name,
 * expiry and who delivers it come from the document-type catalog.
 */
import { useMemo } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { useDocumentTypes } from "@/hooks/useDocuments";
import type { ProcedureTemplate, ProcedureTemplateRow } from "@/types";

export const procedureTemplateKeys = {
  byProcedure: (idTypeProcedure: number) => ["procedureTemplate", "procedureType", idTypeProcedure] as const,
};

export function useProcedureTemplate(idTypeProcedure: number | undefined) {
  const templates = useQuery({
    queryKey: idTypeProcedure !== undefined ? procedureTemplateKeys.byProcedure(idTypeProcedure) : ["procedureTemplate", "procedureType", "none"],
    queryFn: () => apiGet<ProcedureTemplate[]>(`/procedure-templates/procedure-types/${idTypeProcedure}`),
    enabled: idTypeProcedure !== undefined,
  });
  const { data: documentTypes } = useDocumentTypes();
  const data = useMemo<ProcedureTemplateRow[] | undefined>(() => {
    if (!templates.data) return undefined;
    const byId = new Map((documentTypes ?? []).map((d) => [d.idDocumentType, d]));
    return templates.data.map((row) => ({ ...row, documentType: byId.get(row.fkIdDocumentType) }));
  }, [templates.data, documentTypes]);
  return { ...templates, data };
}

export interface RequiredDocumentKey {
  procedureTypeId: number;
  documentTypeId: number;
}

function useInvalidateChecklist() {
  const queryClient = useQueryClient();
  return (procedureTypeId: number) =>
    queryClient.invalidateQueries({ queryKey: procedureTemplateKeys.byProcedure(procedureTypeId) });
}

export function useCreateProcedureTemplate() {
  const invalidate = useInvalidateChecklist();
  return useMutation({
    mutationFn: ({ procedureTypeId, documentTypeId, notes }: RequiredDocumentKey & { notes?: string }) =>
      apiPost<ProcedureTemplate>("/procedure-templates", {
        fkIdProcedureType: procedureTypeId,
        fkIdDocumentType: documentTypeId,
        notes: notes ?? "",
      }),
    onSuccess: (_d, v) => invalidate(v.procedureTypeId),
  });
}

export function useUpdateProcedureTemplate() {
  const invalidate = useInvalidateChecklist();
  return useMutation({
    mutationFn: ({ procedureTypeId, documentTypeId, notes }: RequiredDocumentKey & { notes: string }) =>
      apiPut<ProcedureTemplate>(`/procedure-templates/${procedureTypeId}/${documentTypeId}`, { notes }),
    onSuccess: (_d, v) => invalidate(v.procedureTypeId),
  });
}

export function useDeleteProcedureTemplate() {
  const invalidate = useInvalidateChecklist();
  return useMutation({
    mutationFn: ({ procedureTypeId, documentTypeId }: RequiredDocumentKey) =>
      apiDelete(`/procedure-templates/${procedureTypeId}/${documentTypeId}`),
    onSuccess: (_d, v) => invalidate(v.procedureTypeId),
  });
}
