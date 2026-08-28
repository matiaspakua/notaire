/**
 * React Query hooks for CU43 — reingresar documentación.
 * Endpoints: GET/POST /api/v1/gestiones/{id}/reingreso-documentacion
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api-client";
import type {
  DocumentoReingresado,
  GestionReingresoDocumentacion,
  ReingresoDocumentacionInput,
} from "@/types";

export const reingresoDocumentacionKeys = {
  byGestion: (gestionId: number) => ["reingresoDocumentacion", "gestion", gestionId] as const,
};

export function useReingresoDocumentacion(gestionId: number | undefined) {
  return useQuery({
    queryKey: reingresoDocumentacionKeys.byGestion(gestionId ?? 0),
    queryFn: () => apiGet<GestionReingresoDocumentacion>(`/gestiones/${gestionId}/reingreso-documentacion`),
    enabled: !!gestionId,
  });
}

interface ReingresarDocumentacionInput {
  gestionId: number;
  reingreso: ReingresoDocumentacionInput;
}

export function useReingresarDocumentacion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ gestionId, reingreso }: ReingresarDocumentacionInput) =>
      apiPost<DocumentoReingresado>(`/gestiones/${gestionId}/reingreso-documentacion`, reingreso),
    onSuccess: (_data, { gestionId }) =>
      qc.invalidateQueries({ queryKey: reingresoDocumentacionKeys.byGestion(gestionId) }),
  });
}
