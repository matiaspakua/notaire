/**
 * React Query hook for the gestión workflow trace (dashboard).
 * Fetches GET /api/v1/gestiones/{id}/workflow-trace.
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiGetPage } from "@/lib/api-client";
import { GESTIONES_SORT } from "@/hooks/useGestiones";
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

/** Newest managements the dashboard hero looks through for one with a workflow (#1347). */
export const LATEST_TRACE_CANDIDATES = 20;
/** Traces requested at once while looking. */
const TRACE_BATCH = 5;

export interface LatestTracedGestion {
  gestionId: number;
  trace: GestionWorkflowTrace;
}

/**
 * The newest management that has a workflow (#1347). Draft and test cases
 * often have none (the trace answers 400), so this walks the newest
 * managements in small parallel batches and keeps the first that has one.
 * Null when none of the newest LATEST_TRACE_CANDIDATES has a workflow.
 */
export async function findLatestTracedGestion(): Promise<LatestTracedGestion | null> {
  const page = await apiGetPage<{ idManagement?: number }>("/gestiones", {
    page: 0,
    size: LATEST_TRACE_CANDIDATES,
    sort: GESTIONES_SORT,
  });
  const ids = page.content.map((g) => g.idManagement).filter((id): id is number => id != null);
  for (let i = 0; i < ids.length; i += TRACE_BATCH) {
    const batch = ids.slice(i, i + TRACE_BATCH);
    const results = await Promise.allSettled(
      batch.map((id) => apiGet<GestionWorkflowTrace>(`/gestiones/${id}/workflow-trace`)),
    );
    const hit = results.findIndex((r) => r.status === "fulfilled");
    if (hit >= 0) {
      return { gestionId: batch[hit], trace: (results[hit] as PromiseFulfilledResult<GestionWorkflowTrace>).value };
    }
  }
  return null;
}

export function useLatestTracedGestion(enabled = true) {
  return useQuery({
    queryKey: ["gestion-workflow-trace", "latest"] as const,
    queryFn: findLatestTracedGestion,
    enabled,
    staleTime: 30_000,
  });
}
