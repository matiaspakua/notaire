/**
 * React Query hook for the gestión workflow trace (dashboard).
 * Fetches GET /api/v1/gestiones/{id}/workflow-trace.
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { GestionWorkflowTrace } from "@/types";

export const gestionWorkflowKeys = {
  trace: (gestionId: number) => ["gestion-workflow-trace", gestionId] as const,
};

export function useGestionWorkflowTrace(gestionId: number | undefined) {
  return useQuery({
    queryKey: gestionWorkflowKeys.trace(gestionId ?? 0),
    queryFn: () => apiGet<GestionWorkflowTrace>(`/gestiones/${gestionId}/workflow-trace`),
    enabled: !!gestionId,
  });
}
