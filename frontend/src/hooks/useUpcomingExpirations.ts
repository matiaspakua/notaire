/**
 * React Query hook for CU42 — informar próximos vencimientos.
 * Endpoint: GET /api/v1/submitted-documents/upcoming-expirations?dias=N
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { UpcomingExpiration } from "@/types";

export const upcomingExpirationKeys = {
  byDays: (dias: number) => ["upcomingExpirations", dias] as const,
};

export function useUpcomingExpirations(dias: number) {
  return useQuery({
    queryKey: upcomingExpirationKeys.byDays(dias),
    queryFn: () => apiGet<UpcomingExpiration[]>(`/submitted-documents/upcoming-expirations?dias=${dias}`),
  });
}
