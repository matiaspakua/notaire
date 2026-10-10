/**
 * React Query hooks for RegistroAuditoria (CU — Auditoría del sistema).
 * Endpoints: GET /api/v1/audit-log (paged, newest first; optional module)
 *            GET /api/v1/audit-log/user/{id}
 * Read-only — audit logs are not editable.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { apiGet, apiGetPage } from "@/lib/api-client";
import type { RegistroAuditoria } from "@/types";

export interface AuditoriaPageParams {
  page: number;
  size: number;
  /** Exact module name as stored by the backend (e.g. "People"); omit for all. */
  module?: string;
}

export const auditoriaKeys = {
  all: ["auditoria"] as const,
  page: (params: AuditoriaPageParams) => ["auditoria", "page", params] as const,
  byUsuario: (id: number) => ["auditoria", "usuario", id] as const,
};

/** One server page of the audit log (#1340): the table grows fastest, so it is never loaded whole. */
export function useAuditoria(params: AuditoriaPageParams) {
  return useQuery({
    queryKey: auditoriaKeys.page(params),
    queryFn: () =>
      apiGetPage<RegistroAuditoria>("/audit-log", {
        page: params.page,
        size: params.size,
        sort: "date,desc",
        params: { module: params.module },
      }),
    placeholderData: keepPreviousData,
  });
}

export function useAuditoriaByUsuario(idUsuario: number | null) {
  return useQuery({
    queryKey: auditoriaKeys.byUsuario(idUsuario ?? 0),
    queryFn: () =>
      apiGet<RegistroAuditoria[]>(`/audit-log/user/${idUsuario}`),
    enabled: idUsuario !== null && idUsuario > 0,
  });
}
