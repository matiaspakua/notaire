/**
 * React Query hooks for TipoDeDocumento (CU27, CU65 — Tipos de documento).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/tipo-de-documento
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { TipoDeDocumento } from "@/types";

export const documentosKeys = {
  all: ["tiposDocumento"] as const,
  detail: (id: number) => ["tiposDocumento", id] as const,
};

export function useTiposDocumento() {
  return useQuery({
    queryKey: documentosKeys.all,
    queryFn: () => apiGet<TipoDeDocumento[]>("/tipo-de-documento"),
  });
}
