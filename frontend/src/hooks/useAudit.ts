/**
 * React Query hooks for AuditRecord (CU — Auditoría del sistema).
 * Endpoints: GET /api/v1/audit-log (paged, newest first; optional module)
 *            GET /api/v1/audit-log/user/{id}
 * Read-only — audit logs are not editable.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { apiGet, apiGetPage } from "@/lib/api-client";
import type { AuditRecord } from "@/types";

export interface AuditPageParams {
  page: number;
  size: number;
  /** Exact module name as stored by the backend (e.g. "People"); omit for all. */
  module?: string;
}

export const auditKeys = {
  all: ["audit"] as const,
  page: (params: AuditPageParams) => ["audit", "page", params] as const,
  byUser: (id: number) => ["audit", "user", id] as const,
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
        params: { module: params.module },
      }),
    placeholderData: keepPreviousData,
  });
}

export function useAuditByUser(idUsuario: number | null) {
  return useQuery({
    queryKey: auditKeys.byUser(idUsuario ?? 0),
    queryFn: () =>
      apiGet<AuditRecord[]>(`/audit-log/user/${idUsuario}`),
    enabled: idUsuario !== null && idUsuario > 0,
  });
}
