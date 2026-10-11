/**
 * React Query hooks for CU10 — movements of external-entity documents.
 * Endpoints: GET/PUT /api/v1/managements/{id}/external-entity-documents[/{idSubmittedDocument}]
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPut } from "@/lib/api-client";
import type { ManagementExternalEntityDocuments, ExternalEntityDocumentMovementInput } from "@/types";

export const externalEntityDocumentKeys = {
  byManagement: (managementId: number) => ["externalEntityDocuments", "management", managementId] as const,
};

export function useExternalEntityDocuments(managementId: number | undefined) {
  return useQuery({
    queryKey: externalEntityDocumentKeys.byManagement(managementId ?? 0),
    queryFn: () => apiGet<ManagementExternalEntityDocuments>(`/managements/${managementId}/external-entity-documents`),
    enabled: !!managementId,
  });
}

interface RegisterMovementInput {
  managementId: number;
  idSubmittedDocument: number;
  movement: ExternalEntityDocumentMovementInput;
}

export function useRegisterExternalEntityDocumentMovement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ managementId, idSubmittedDocument, movement }: RegisterMovementInput) =>
      apiPut(`/managements/${managementId}/external-entity-documents/${idSubmittedDocument}`, movement),
    onSuccess: (_data, { managementId }) =>
      qc.invalidateQueries({ queryKey: externalEntityDocumentKeys.byManagement(managementId) }),
  });
}
