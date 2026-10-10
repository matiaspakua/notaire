/**
 * React Query hooks for ManagementStatus (CU67 — Gestionar estados de gestión).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/management-statuses
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { ManagementStatus } from "@/types";

export const estadosGestionKeys = {
  all: ["estadosGestion"] as const,
  detail: (id: number) => ["estadosGestion", id] as const,
};

export function useManagementStatuses() {
  return useQuery({
    queryKey: estadosGestionKeys.all,
    queryFn: () => apiGet<ManagementStatus[]>("/management-statuses"),
  });
}
