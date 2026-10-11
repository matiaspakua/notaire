/**
 * React Query hooks for CU10 — movements of external-entity documents.
 * Endpoints: GET/PUT /api/v1/managements/{id}/external-entity-documents[/{idDocumentPresentado}]
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPut } from "@/lib/api-client";
import type { ManagementExternalEntityDocuments, ExternalEntityDocumentMovementInput } from "@/types";

export const externalEntityDocumentKeys = {
  byManagement: (managementId: number) => ["documentosEntidadExterna", "gestion", managementId] as const,
};

export function useExternalEntityDocuments(managementId: number | undefined) {
  return useQuery({
    queryKey: externalEntityDocumentKeys.byManagement(managementId ?? 0),
    queryFn: () => apiGet<ManagementExternalEntityDocuments>(`/managements/${managementId}/external-entity-documents`),
    enabled: !!managementId,
  });
}

interface RegistrarMovementInput {
  managementId: number;
  idDocumentPresentado: number;
  movement: ExternalEntityDocumentMovementInput;
}

export function useRegisterExternalEntityDocumentMovement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ managementId, idDocumentPresentado, movement }: RegistrarMovementInput) =>
      apiPut(`/managements/${managementId}/external-entity-documents/${idDocumentPresentado}`, movement),
    onSuccess: (_data, { managementId }) =>
      qc.invalidateQueries({ queryKey: externalEntityDocumentKeys.byManagement(managementId) }),
  });
}
