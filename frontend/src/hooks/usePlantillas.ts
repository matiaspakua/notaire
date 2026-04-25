/**
 * React Query hooks for PlantillaPresupuesto (CU39, CU49, CU55 — Budget templates).
 * Endpoints: GET/POST /api/v1/plantilla-presupuestos
 *            PUT/DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{id}/concepto/{id}
 *            GET /api/v1/plantilla-presupuestos/tipo-tramite/{id}
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { PlantillaPresupuesto } from "@/types";

export const plantillasKeys = {
  all: ["plantillas"] as const,
  byTipoTramite: (id: number) => ["plantillas", "tipoTramite", id] as const,
};

export function usePlantillas() {
  return useQuery({
    queryKey: plantillasKeys.all,
    queryFn: () => apiGet<PlantillaPresupuesto[]>("/plantilla-presupuestos"),
  });
}

export function usePlantillasByTipoTramite(idTipoTramite: number | null) {
  return useQuery({
    queryKey: plantillasKeys.byTipoTramite(idTipoTramite ?? 0),
    queryFn: () =>
      apiGet<PlantillaPresupuesto[]>(
        `/plantilla-presupuestos/tipo-tramite/${idTipoTramite}`
      ),
    enabled: idTipoTramite !== null && idTipoTramite > 0,
  });
}

export function useCreatePlantilla() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<PlantillaPresupuesto>) =>
      apiPost<void>("/plantilla-presupuestos", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: plantillasKeys.all }),
  });
}

export function useUpdatePlantilla() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      idTipoTramite,
      idConcepto,
      data,
    }: {
      idTipoTramite: number;
      idConcepto: number;
      data: Partial<PlantillaPresupuesto>;
    }) =>
      apiPut<void>(
        `/plantilla-presupuestos/tipo-tramite/${idTipoTramite}/concepto/${idConcepto}`,
        data
      ),
    onSuccess: () => qc.invalidateQueries({ queryKey: plantillasKeys.all }),
  });
}

export function useDeletePlantilla() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      idTipoTramite,
      idConcepto,
    }: {
      idTipoTramite: number;
      idConcepto: number;
    }) =>
      apiDelete(
        `/plantilla-presupuestos/tipo-tramite/${idTipoTramite}/concepto/${idConcepto}`
      ),
    onSuccess: () => qc.invalidateQueries({ queryKey: plantillasKeys.all }),
  });
}
