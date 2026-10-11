/**
 * React Query hook for ProcedureTemplate (CU03 — documents required for a procedure).
 * Endpoint: GET /api/v1/procedure-templates/procedure-types/{idTypeProcedure}
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { ProcedureTemplate } from "@/types";

export const procedureTemplateKeys = {
  byProcedure: (idTypeProcedure: number) => ["procedureTemplate", "procedureType", idTypeProcedure] as const,
};

export function useProcedureTemplate(idTypeProcedure: number | undefined) {
  return useQuery({
    queryKey: idTypeProcedure !== undefined ? procedureTemplateKeys.byProcedure(idTypeProcedure) : ["procedureTemplate", "procedureType", "none"],
    queryFn: () => apiGet<ProcedureTemplate[]>(`/procedure-templates/procedure-types/${idTypeProcedure}`),
    enabled: idTypeProcedure !== undefined,
  });
}
