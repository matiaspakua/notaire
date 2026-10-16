/**
 * React Query hooks for AuditRecord (system audit).
 * Hexagon wiring: #1843 / ADR-030.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { auditUseCases } from "@/composition/audit";
import type { AuditRecord as DomainAudit } from "@/core/audit/domain/audit";
import type { AuditRecord } from "@/types";

export interface AuditPageParams {
  page: number;
  size: number;
  /** Exact module name as stored by the backend (e.g. "People"); omit for all. */
  module?: string;
  /** #1545: only this user's records. */
  idUser?: number;
  /** #1545: inclusive yyyy-MM-dd bounds (business time zone). */
  from?: string;
  to?: string;
}

export const auditKeys = {
  all: ["audit"] as const,
  page: (params: AuditPageParams) => ["audit", "page", params] as const,
};

function toWire(r: DomainAudit): AuditRecord {
  return r as AuditRecord;
}

/** One server page of the audit log (#1340): the table grows fastest, so it is never loaded whole. */
export function useAudit(params: AuditPageParams) {
  return useQuery({
    queryKey: auditKeys.page(params),
    queryFn: async () => {
      const page = await auditUseCases.listPage(params);
      return {
        ...page,
        content: page.content.map(toWire),
      };
    },
    placeholderData: keepPreviousData,
  });
}
