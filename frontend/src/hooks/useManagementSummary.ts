/**
 * React Query hooks for the management case summary (#774) and its financial summary (CU47/CU02).
 * Hexagon wiring: #1839 / ADR-030.
 */
import { useQuery } from "@tanstack/react-query";
import { managementUseCases } from "@/composition/management";
import type {
  ManagementCaseSummary as DomainCaseSummary,
  ManagementFinancialSummary as DomainFinancialSummary,
} from "@/core/management/domain/management";
import type { ManagementCaseSummary, ManagementFinancialSummary } from "@/types";

export const managementSummaryKeys = {
  caseSummary: (managementId: number) => ["management-case-summary", managementId] as const,
  financialSummary: (managementId: number) => ["management-financial-summary", managementId] as const,
};

function toCaseSummary(s: DomainCaseSummary): ManagementCaseSummary {
  return s as ManagementCaseSummary;
}

function toFinancialSummary(s: DomainFinancialSummary): ManagementFinancialSummary {
  return s;
}

export function useManagementCaseSummary(managementId: number | undefined) {
  return useQuery({
    queryKey: managementSummaryKeys.caseSummary(managementId ?? 0),
    queryFn: async () => toCaseSummary(await managementUseCases.getCaseSummary(managementId!)),
    enabled: !!managementId,
  });
}

export function useManagementFinancialSummary(managementId: number | undefined) {
  return useQuery({
    queryKey: managementSummaryKeys.financialSummary(managementId ?? 0),
    queryFn: async () => toFinancialSummary(await managementUseCases.getFinancialSummary(managementId!)),
    enabled: !!managementId,
  });
}
