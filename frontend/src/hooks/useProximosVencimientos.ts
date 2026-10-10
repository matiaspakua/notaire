/**
 * React Query hook for CU42 — informar próximos vencimientos.
 * Endpoint: GET /api/v1/submitted-documents/upcoming-expirations?dias=N
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { UpcomingExpiration } from "@/types";

export const proximosVencimientosKeys = {
  byDias: (dias: number) => ["proximosVencimientos", dias] as const,
};

export function useProximosVencimientos(dias: number) {
  return useQuery({
    queryKey: proximosVencimientosKeys.byDias(dias),
    queryFn: () => apiGet<UpcomingExpiration[]>(`/submitted-documents/upcoming-expirations?dias=${dias}`),
  });
}
