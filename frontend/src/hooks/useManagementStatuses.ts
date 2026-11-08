/**
 * React Query hooks for ManagementStatus (CU67 — manage management statuses).
 * Hexagon wiring: #1769 / ADR-030.
 * Endpoint: GET/POST/PUT/DELETE /api/v1/management-statuses
 */
import { useQuery } from "@tanstack/react-query";
import { managementStatusUseCases } from "@/composition/management-status";
import type { ManagementStatus as DomainManagementStatus } from "@/core/management-status/domain/management-status";
import type { ManagementStatus } from "@/types";

export const managementStatusKeys = {
  all: ["managementStatuses"] as const,
  detail: (id: number) => ["managementStatuses", id] as const,
};

/** Map domain model back to the wire-facing UI type used across the app. */
function toWire(status: DomainManagementStatus): ManagementStatus {
  return {
    idManagementStatus: status.id,
    name: status.name,
    notes: status.notes ?? undefined,
    version: status.version,
  };
}

export function useManagementStatuses() {
  return useQuery({
    queryKey: managementStatusKeys.all,
    queryFn: async () => (await managementStatusUseCases.list()).map(toWire),
  });
}
