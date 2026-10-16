/**
 * React Query hook for the management workflow trace (dashboard).
 * Fetches GET /api/v1/managements/{id}/workflow-trace.
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiGetPage } from "@/lib/api-client";
import { MANAGEMENTS_SORT } from "@/hooks/useManagements";
import type { ManagementWorkflowTrace } from "@/types";

export const managementWorkflowKeys = {
  trace: (managementId: number) => ["management-workflow-trace", managementId] as const,
  latest: ["management-workflow-trace", "latest"] as const,
};

export function useManagementWorkflowTrace(managementId: number | undefined) {
  return useQuery({
    queryKey: managementWorkflowKeys.trace(managementId ?? 0),
    queryFn: () => apiGet<ManagementWorkflowTrace>(`/managements/${managementId}/workflow-trace`),
    enabled: !!managementId,
  });
}

/** Newest managements the dashboard hero looks through for one with a workflow (#1347). */
export const LATEST_TRACE_CANDIDATES = 20;
/** Traces requested at once while looking. */
const TRACE_BATCH = 5;

export interface LatestTracedManagement {
  managementId: number;
  trace: ManagementWorkflowTrace;
}

/**
 * The newest management that has a workflow (#1347). Draft and test cases
 * often have none (the trace answers 400), so this walks the newest
 * managements in small parallel batches and keeps the first that has one.
 * Null when none of the newest LATEST_TRACE_CANDIDATES has a workflow.
 */
export async function findLatestTracedManagement(): Promise<LatestTracedManagement | null> {
  const page = await apiGetPage<{ idManagement?: number }>("/managements", {
    page: 0,
    size: LATEST_TRACE_CANDIDATES,
    sort: MANAGEMENTS_SORT,
  });
  const ids = page.content.map((g) => g.idManagement).filter((id): id is number => id != null);
  for (let i = 0; i < ids.length; i += TRACE_BATCH) {
    const batch = ids.slice(i, i + TRACE_BATCH);
    const results = await Promise.allSettled(
      batch.map((id) => apiGet<ManagementWorkflowTrace>(`/managements/${id}/workflow-trace`)),
    );
    const hit = results.findIndex((r) => r.status === "fulfilled");
    if (hit >= 0) {
      return { managementId: batch[hit], trace: (results[hit] as PromiseFulfilledResult<ManagementWorkflowTrace>).value };
    }
  }
  return null;
}

export function useLatestTracedManagement(enabled = true) {
  return useQuery({
    queryKey: managementWorkflowKeys.latest,
    queryFn: findLatestTracedManagement,
    enabled,
    staleTime: 30_000,
  });
}
