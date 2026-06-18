/**
 * React Query hooks for EstadoDeGestion (CU67 — Gestionar estados de gestión).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/estado-gestion
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { EstadoDeGestion } from "@/types";

export const estadosGestionKeys = {
  all: ["estadosGestion"] as const,
  detail: (id: number) => ["estadosGestion", id] as const,
};

export function useEstadosGestion() {
  return useQuery({
    queryKey: estadosGestionKeys.all,
    queryFn: () => apiGet<EstadoDeGestion[]>("/estado-gestion"),
  });
}
