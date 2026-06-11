/**
 * React Query hooks for RegistroAuditoria (CU — Auditoría del sistema).
 * Endpoints: GET /api/v1/registro-auditoria
 *            GET /api/v1/registro-auditoria/usuario/{id}
 * Read-only — audit logs are not editable.
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiGetPaged } from "@/lib/api-client";
import type { RegistroAuditoria } from "@/types";

export const auditoriaKeys = {
  all: ["auditoria"] as const,
  byUsuario: (id: number) => ["auditoria", "usuario", id] as const,
};

export function useAuditoria() {
  return useQuery({
    queryKey: auditoriaKeys.all,
    queryFn: () => apiGetPaged<RegistroAuditoria>("/registro-auditoria"),
  });
}

export function useAuditoriaByUsuario(idUsuario: number | null) {
  return useQuery({
    queryKey: auditoriaKeys.byUsuario(idUsuario ?? 0),
    queryFn: () =>
      apiGet<RegistroAuditoria[]>(`/registro-auditoria/usuario/${idUsuario}`),
    enabled: idUsuario !== null && idUsuario > 0,
  });
}
