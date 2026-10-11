/**
 * React Query hooks for the gestión case summary (#774) and its financial summary (CU47/CU02).
 * Endpoints: GET /api/v1/managements/{id}/case-summary and /financial-summary
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { ManagementCaseSummary, ManagementFinancialSummary } from "@/types";

export const managementSummaryKeys = {
  caso: (gestionId: number) => ["management-case-summary", gestionId] as const,
  financiero: (gestionId: number) => ["management-financial-summary", gestionId] as const,
};

export function useManagementCaseSummary(gestionId: number | undefined) {
  return useQuery({
    queryKey: managementSummaryKeys.caso(gestionId ?? 0),
    queryFn: () => apiGet<ManagementCaseSummary>(`/managements/${gestionId}/case-summary`),
    enabled: !!gestionId,
  });
}

export function useManagementFinancialSummary(gestionId: number | undefined) {
  return useQuery({
    queryKey: managementSummaryKeys.financiero(gestionId ?? 0),
    queryFn: () => apiGet<ManagementFinancialSummary>(`/managements/${gestionId}/financial-summary`),
    enabled: !!gestionId,
  });
}
