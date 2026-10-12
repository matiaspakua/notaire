/**
 * React Query hooks for AuditRecord (system audit).
 * Endpoints: GET /api/v1/audit-log (paged, newest first; optional module, idUser and an
 *            inclusive from/to day range, #1545 / CU23)
 * Read-only — audit logs are not editable.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { apiGetPage } from "@/lib/api-client";
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

/** One server page of the audit log (#1340): the table grows fastest, so it is never loaded whole. */
export function useAudit(params: AuditPageParams) {
  return useQuery({
    queryKey: auditKeys.page(params),
    queryFn: () =>
      apiGetPage<AuditRecord>("/audit-log", {
        page: params.page,
        size: params.size,
        sort: "date,desc",
        params: { module: params.module, idUser: params.idUser, from: params.from, to: params.to },
      }),
    placeholderData: keepPreviousData,
  });
}
