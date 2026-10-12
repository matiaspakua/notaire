/**
 * React Query hook for CU42 — upcoming document expirations.
 * Endpoint: GET /api/v1/submitted-documents/upcoming-expirations?days=N
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { UpcomingExpiration } from "@/types";

/** Window shared by the upcoming-expirations page and the dashboard alert (#1556). */
export const UPCOMING_EXPIRATIONS_DEFAULT_DAYS = 30;

export const upcomingExpirationKeys = {
  byDays: (days: number) => ["upcomingExpirations", days] as const,
};

export function useUpcomingExpirations(days: number) {
  return useQuery({
    queryKey: upcomingExpirationKeys.byDays(days),
    queryFn: () => apiGet<UpcomingExpiration[]>(`/submitted-documents/upcoming-expirations?days=${days}`),
  });
}
