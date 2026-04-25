import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Pago } from "@/types";

export const pagosKeys = { all: ["pagos"] as const };

export function usePagos() {
  return useQuery({ queryKey: pagosKeys.all, queryFn: () => apiGet<Pago[]>("/pagos") });
}
export function useCreatePago() {
  const qc = useQueryClient();
  return useMutation({ mutationFn: (d: Partial<Pago>) => apiPost<void>("/pagos", d), onSuccess: () => qc.invalidateQueries({ queryKey: pagosKeys.all }) });
}
export function useUpdatePago() {
  const qc = useQueryClient();
  return useMutation({ mutationFn: ({ id, data }: { id: number; data: Partial<Pago> }) => apiPut<void>(`/pagos/${id}`, data), onSuccess: () => qc.invalidateQueries({ queryKey: pagosKeys.all }) });
}
export function useDeletePago() {
  const qc = useQueryClient();
  return useMutation({ mutationFn: (id: number) => apiDelete(`/pagos/${id}`), onSuccess: () => qc.invalidateQueries({ queryKey: pagosKeys.all }) });
}
