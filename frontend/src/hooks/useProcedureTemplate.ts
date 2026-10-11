/**
 * React Query hook for ProcedureTemplate (CU03 — documentos necesarios por trámite).
 * Endpoint: GET /api/v1/procedure-templates/procedure-types/{idTipoTramite}
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { ProcedureTemplate } from "@/types";

export const procedureTemplateKeys = {
  byTramite: (idTipoTramite: number) => ["plantillaTramite", "tramite", idTipoTramite] as const,
};

export function useProcedureTemplate(idTipoTramite: number | undefined) {
  return useQuery({
    queryKey: idTipoTramite !== undefined ? procedureTemplateKeys.byTramite(idTipoTramite) : ["plantillaTramite", "tramite", "none"],
    queryFn: () => apiGet<ProcedureTemplate[]>(`/procedure-templates/procedure-types/${idTipoTramite}`),
    enabled: idTipoTramite !== undefined,
  });
}
