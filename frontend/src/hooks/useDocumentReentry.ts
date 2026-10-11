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
  byManagement: (managementId: number) => ["reingresoDocumentacion", "gestion", managementId] as const,
};

export function useDocumentReentry(managementId: number | undefined) {
  return useQuery({
    queryKey: documentReentryKeys.byManagement(managementId ?? 0),
    queryFn: () => apiGet<ManagementDocumentReentry>(`/managements/${managementId}/document-reentry`),
    enabled: !!managementId,
  });
}

interface ReingresarDocumentacionInput {
  managementId: number;
  reingreso: DocumentReentryInput;
}

export function useReenterDocumentation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ managementId, reingreso }: ReingresarDocumentacionInput) =>
      apiPost<ReenteredDocument>(`/managements/${managementId}/document-reentry`, reingreso),
    onSuccess: (_data, { managementId }) =>
      qc.invalidateQueries({ queryKey: documentReentryKeys.byManagement(managementId) }),
  });
}
