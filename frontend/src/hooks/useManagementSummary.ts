/**
 * React Query hooks for the gestión case summary (#774) and its financial summary (CU47/CU02).
 * Endpoints: GET /api/v1/managements/{id}/case-summary and /financial-summary
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { ManagementCaseSummary, ManagementFinancialSummary } from "@/types";

export const managementSummaryKeys = {
  caso: (managementId: number) => ["management-case-summary", managementId] as const,
  financiero: (managementId: number) => ["management-financial-summary", managementId] as const,
};

export function useManagementCaseSummary(managementId: number | undefined) {
  return useQuery({
    queryKey: managementSummaryKeys.caso(managementId ?? 0),
    queryFn: () => apiGet<ManagementCaseSummary>(`/managements/${managementId}/case-summary`),
    enabled: !!managementId,
  });
}

export function useManagementFinancialSummary(managementId: number | undefined) {
  return useQuery({
    queryKey: managementSummaryKeys.financiero(managementId ?? 0),
    queryFn: () => apiGet<ManagementFinancialSummary>(`/managements/${managementId}/financial-summary`),
    enabled: !!managementId,
  });
}
