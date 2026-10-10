/**
 * React Query hooks for CU10 — movimientos de documentación de entidades externas.
 * Endpoints: GET/PUT /api/v1/managements/{id}/external-entity-documents[/{idDocumentoPresentado}]
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPut } from "@/lib/api-client";
import type { ManagementExternalEntityDocuments, ExternalEntityDocumentMovementInput } from "@/types";

export const documentosEntidadExternaKeys = {
  byGestion: (gestionId: number) => ["documentosEntidadExterna", "gestion", gestionId] as const,
};

export function useExternalEntityDocuments(gestionId: number | undefined) {
  return useQuery({
    queryKey: documentosEntidadExternaKeys.byGestion(gestionId ?? 0),
    queryFn: () => apiGet<ManagementExternalEntityDocuments>(`/managements/${gestionId}/external-entity-documents`),
    enabled: !!gestionId,
  });
}

interface RegistrarMovimientoInput {
  gestionId: number;
  idDocumentoPresentado: number;
  movimiento: ExternalEntityDocumentMovementInput;
}

export function useRegistrarMovimientoDocumentoEntidadExterna() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ gestionId, idDocumentoPresentado, movimiento }: RegistrarMovimientoInput) =>
      apiPut(`/managements/${gestionId}/external-entity-documents/${idDocumentoPresentado}`, movimiento),
    onSuccess: (_data, { gestionId }) =>
      qc.invalidateQueries({ queryKey: documentosEntidadExternaKeys.byGestion(gestionId) }),
  });
}
