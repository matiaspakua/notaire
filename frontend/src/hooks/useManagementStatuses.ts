/**
 * React Query hooks for ManagementStatus (CU67 — manage management statuses).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/management-statuses
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { ManagementStatus } from "@/types";

export const managementStatusKeys = {
  all: ["managementStatuses"] as const,
  detail: (id: number) => ["managementStatuses", id] as const,
};

export function useManagementStatuses() {
  return useQuery({
    queryKey: managementStatusKeys.all,
    queryFn: () => apiGet<ManagementStatus[]>("/management-statuses"),
  });
}
