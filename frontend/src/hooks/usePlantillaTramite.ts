/**
 * React Query hook for PlantillaTramite (CU03 — documentos necesarios por trámite).
 * Endpoint: GET /api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { PlantillaTramite } from "@/types";

export const plantillaTramiteKeys = {
  byTramite: (idTipoTramite: number) => ["plantillaTramite", "tramite", idTipoTramite] as const,
};

export function usePlantillaTramite(idTipoTramite: number | undefined) {
  return useQuery({
    queryKey: idTipoTramite !== undefined ? plantillaTramiteKeys.byTramite(idTipoTramite) : ["plantillaTramite", "tramite", "none"],
    queryFn: () => apiGet<PlantillaTramite[]>(`/plantilla-tramite/tipo-tramite/${idTipoTramite}`),
    enabled: idTipoTramite !== undefined,
  });
}
