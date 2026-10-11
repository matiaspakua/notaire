/**
 * React Query hooks for CU43 — reingresar documentación.
 * Endpoints: GET/POST /api/v1/managements/{id}/document-reentry
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api-client";
import type {
  ReenteredDocument,
  ManagementDocumentReentry,
  DocumentReentryInput,
} from "@/types";

export const documentReentryKeys = {
  byManagement: (gestionId: number) => ["reingresoDocumentacion", "gestion", gestionId] as const,
};

export function useDocumentReentry(gestionId: number | undefined) {
  return useQuery({
    queryKey: documentReentryKeys.byManagement(gestionId ?? 0),
    queryFn: () => apiGet<ManagementDocumentReentry>(`/managements/${gestionId}/document-reentry`),
    enabled: !!gestionId,
  });
}

interface ReingresarDocumentacionInput {
  gestionId: number;
  reingreso: DocumentReentryInput;
}

export function useReenterDocumentation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ gestionId, reingreso }: ReingresarDocumentacionInput) =>
      apiPost<ReenteredDocument>(`/managements/${gestionId}/document-reentry`, reingreso),
    onSuccess: (_data, { gestionId }) =>
      qc.invalidateQueries({ queryKey: documentReentryKeys.byManagement(gestionId) }),
  });
}
