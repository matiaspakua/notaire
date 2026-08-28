/**
 * React Query hooks for CU10 — movimientos de documentación de entidades externas.
 * Endpoints: GET/PUT /api/v1/gestiones/{id}/documentos-entidades-externas[/{idDocumentoPresentado}]
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPut } from "@/lib/api-client";
import type { GestionDocumentosEntidadesExternas, MovimientoDocumentoEntidadExternaInput } from "@/types";

export const documentosEntidadExternaKeys = {
  byGestion: (gestionId: number) => ["documentosEntidadExterna", "gestion", gestionId] as const,
};

export function useDocumentosEntidadExterna(gestionId: number | undefined) {
  return useQuery({
    queryKey: documentosEntidadExternaKeys.byGestion(gestionId ?? 0),
    queryFn: () => apiGet<GestionDocumentosEntidadesExternas>(`/gestiones/${gestionId}/documentos-entidades-externas`),
    enabled: !!gestionId,
  });
}

interface RegistrarMovimientoInput {
  gestionId: number;
  idDocumentoPresentado: number;
  movimiento: MovimientoDocumentoEntidadExternaInput;
}

export function useRegistrarMovimientoDocumentoEntidadExterna() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ gestionId, idDocumentoPresentado, movimiento }: RegistrarMovimientoInput) =>
      apiPut(`/gestiones/${gestionId}/documentos-entidades-externas/${idDocumentoPresentado}`, movimiento),
    onSuccess: (_data, { gestionId }) =>
      qc.invalidateQueries({ queryKey: documentosEntidadExternaKeys.byGestion(gestionId) }),
  });
}
