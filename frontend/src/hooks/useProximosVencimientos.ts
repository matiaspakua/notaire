/**
 * React Query hook for CU42 — informar próximos vencimientos.
 * Endpoint: GET /api/v1/documento-presentado/proximos-vencimientos?dias=N
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { ProximoVencimiento } from "@/types";

export const proximosVencimientosKeys = {
  byDias: (dias: number) => ["proximosVencimientos", dias] as const,
};

export function useProximosVencimientos(dias: number) {
  return useQuery({
    queryKey: proximosVencimientosKeys.byDias(dias),
    queryFn: () => apiGet<ProximoVencimiento[]>(`/documento-presentado/proximos-vencimientos?dias=${dias}`),
  });
}
