/**
 * React Query hook for CU42 — upcoming document expirations.
 * Hexagon wiring: #1841 / ADR-030.
 */
import { useQuery } from "@tanstack/react-query";
import { submittedDocumentUseCases } from "@/composition/submitted-document";
import type { UpcomingExpiration as DomainUpcomingExpiration } from "@/core/submitted-document/domain/submitted-document";
import type { UpcomingExpiration } from "@/types";

/** Window shared by the upcoming-expirations page and the dashboard alert (#1556). */
export const UPCOMING_EXPIRATIONS_DEFAULT_DAYS = 30;

export const upcomingExpirationKeys = {
  byDays: (days: number) => ["upcomingExpirations", days] as const,
};

function toWire(row: DomainUpcomingExpiration): UpcomingExpiration {
  return row;
}

export function useUpcomingExpirations(days: number) {
  return useQuery({
    queryKey: upcomingExpirationKeys.byDays(days),
    queryFn: async () =>
      (await submittedDocumentUseCases.listUpcomingExpirations(days)).map(toWire),
  });
}
