/**
 * React Query hooks for the gestión case summary (#774) and its financial summary (CU47/CU02).
 * Endpoints: GET /api/v1/gestiones/{id}/resumen-caso and /resumen-financiero
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { GestionResumenCaso, GestionResumenFinanciero } from "@/types";

export const gestionResumenKeys = {
  caso: (gestionId: number) => ["gestion-resumen-caso", gestionId] as const,
  financiero: (gestionId: number) => ["gestion-resumen-financiero", gestionId] as const,
};

export function useGestionResumenCaso(gestionId: number | undefined) {
  return useQuery({
    queryKey: gestionResumenKeys.caso(gestionId ?? 0),
    queryFn: () => apiGet<GestionResumenCaso>(`/gestiones/${gestionId}/resumen-caso`),
    enabled: !!gestionId,
  });
}

export function useGestionResumenFinanciero(gestionId: number | undefined) {
  return useQuery({
    queryKey: gestionResumenKeys.financiero(gestionId ?? 0),
    queryFn: () => apiGet<GestionResumenFinanciero>(`/gestiones/${gestionId}/resumen-financiero`),
    enabled: !!gestionId,
  });
}
