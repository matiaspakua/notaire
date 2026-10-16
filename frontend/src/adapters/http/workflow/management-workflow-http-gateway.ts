/**
 * HTTP adapter for management workflow traces (#1792 / ADR-030).
 * Only place in this surface that may import api-client for management workflow-trace.
 */
import { apiGet, apiGetPage } from "@/lib/api-client";
import type { ManagementWorkflowGateway } from "@/core/workflow/application/ports";
import type {
  LatestTracedManagement,
  ManagementWorkflowTrace,
} from "@/core/workflow/domain/workflow";

export const managementWorkflowHttpGateway: ManagementWorkflowGateway = {
  getTrace: (managementId) =>
    apiGet<ManagementWorkflowTrace>(`/managements/${managementId}/workflow-trace`),

  listManagementIds: async (page, size, sort) => {
    const result = await apiGetPage<{ idManagement?: number }>("/managements", {
      page,
      size,
      sort,
    });
    return { content: result.content };
  },

  findLatestTraced: async (candidateLimit, batchSize, sort) => {
    const page = await managementWorkflowHttpGateway.listManagementIds(0, candidateLimit, sort);
    const ids = page.content.map((g) => g.idManagement).filter((id): id is number => id != null);
    for (let i = 0; i < ids.length; i += batchSize) {
      const batch = ids.slice(i, i + batchSize);
      const results = await Promise.allSettled(
        batch.map((id) => managementWorkflowHttpGateway.getTrace(id)),
      );
      const hit = results.findIndex((r) => r.status === "fulfilled");
      if (hit >= 0) {
        return {
          managementId: batch[hit],
          trace: (results[hit] as PromiseFulfilledResult<ManagementWorkflowTrace>).value,
        } satisfies LatestTracedManagement;
      }
    }
    return null;
  },
};
