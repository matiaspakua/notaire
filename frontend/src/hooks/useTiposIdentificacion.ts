/**
 * Identification type catalog (DNI, CUIT, Pasaporte, …) for the person form (#1361, RF-37).
 * Endpoint: GET /api/v1/tipo-identificacion
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { TipoIdentificacion } from "@/types";

export const tiposIdentificacionKeys = {
  all: ["tiposIdentificacion"] as const,
};

/** Name of the type a new person starts with (Owner decision on #1361). */
export const DEFAULT_IDENTIFICATION_TYPE = "DNI";

export function useTiposIdentificacion() {
  return useQuery({
    queryKey: tiposIdentificacionKeys.all,
    queryFn: () => apiGet<TipoIdentificacion[]>("/tipo-identificacion"),
    staleTime: 5 * 60 * 1000,
  });
}

/** The id of the default type (DNI, any letter case) in the catalog, if present. */
export function defaultIdentificationTypeId(types: TipoIdentificacion[] | undefined): number | undefined {
  return types?.find((t) => t.name?.trim().toUpperCase() === DEFAULT_IDENTIFICATION_TYPE)?.idIdentificationType;
}
