/**
 * React Query hook for the management workflow trace (dashboard).
 * Hexagon wiring: #1792 / ADR-030.
 */
import { useQuery } from "@tanstack/react-query";
import { managementWorkflowUseCases } from "@/composition/workflow";
import { MANAGEMENTS_SORT } from "@/hooks/useManagements";
import type { ManagementWorkflowTrace } from "@/types";

export const managementWorkflowKeys = {
  trace: (managementId: number) => ["management-workflow-trace", managementId] as const,
  latest: ["management-workflow-trace", "latest"] as const,
};

export function useManagementWorkflowTrace(managementId: number | undefined) {
  return useQuery({
    queryKey: managementWorkflowKeys.trace(managementId ?? 0),
    queryFn: () =>
      managementWorkflowUseCases.getTrace(managementId!) as Promise<ManagementWorkflowTrace>,
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
  const hit = await managementWorkflowUseCases.findLatestTraced(
    LATEST_TRACE_CANDIDATES,
    TRACE_BATCH,
    MANAGEMENTS_SORT,
  );
  return hit as LatestTracedManagement | null;
}

export function useLatestTracedManagement(enabled = true) {
  return useQuery({
    queryKey: managementWorkflowKeys.latest,
    queryFn: findLatestTracedManagement,
    enabled,
    staleTime: 30_000,
  });
}
