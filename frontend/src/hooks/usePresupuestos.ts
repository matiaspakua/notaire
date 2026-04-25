import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Presupuesto } from "@/types";

export const presupuestosKeys = {
  all: ["presupuestos"] as const,
  detail: (id: number) => ["presupuestos", id] as const,
  byPersona: (id: number) => ["presupuestos", "persona", id] as const,
};

export function usePresupuestos() {
  return useQuery({
    queryKey: presupuestosKeys.all,
    queryFn: () => apiGet<Presupuesto[]>("/presupuestos"),
  });
}

export function usePresupuesto(id: number) {
  return useQuery({
    queryKey: presupuestosKeys.detail(id),
    queryFn: () => apiGet<Presupuesto>(`/presupuestos/${id}`),
    enabled: id > 0,
  });
}

export function usePresupuestosByPersona(idPersona: number) {
  return useQuery({
    queryKey: presupuestosKeys.byPersona(idPersona),
    queryFn: () => apiGet<Presupuesto[]>(`/presupuestos/persona/${idPersona}`),
    enabled: idPersona > 0,
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
