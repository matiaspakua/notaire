import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Presupuesto, PresupuestoResumen } from "@/types";

export const presupuestosKeys = {
  all: ["presupuestos"] as const,
  detail: (id: number) => ["presupuestos", id] as const,
  byPersona: (id: number) => ["presupuestos", "persona", id] as const,
  resumen: (id: number) => ["presupuestos", id, "resumen"] as const,
};

/** CU47 - Consultar Pago: financial summary (gestión, total, saldo, pagos) for a presupuesto. */
export function usePresupuestoResumen(id: number | null) {
  return useQuery({
    queryKey: presupuestosKeys.resumen(id ?? 0),
    queryFn: () => apiGet<PresupuestoResumen>(`/presupuestos/${id}/resumen`),
    enabled: id !== null,
  });
}

export function usePresupuestos() {
  return useQuery({
    queryKey: presupuestosKeys.all,
    queryFn: () => apiGetPaged<Presupuesto>("/presupuestos"),
  });
}

export function useCreatePresupuesto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Presupuesto>) => apiPost<void>("/presupuestos", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: presupuestosKeys.all }),
  });
}

export function useUpdatePresupuesto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Presupuesto> }) =>
      apiPut<void>(`/presupuestos/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: presupuestosKeys.all }),
  });
}

export function useDeletePresupuesto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/presupuestos/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: presupuestosKeys.all }),
  });
}
