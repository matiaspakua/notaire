import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Pago } from "@/types";

export const pagosKeys = { all: ["pagos"] as const };

export function usePagos() {
  return useQuery({ queryKey: pagosKeys.all, queryFn: () => apiGet<Pago[]>("/pagos") });
}
// Values match PaymentStatus.java verbatim — its constant names are the published REST
// contract (see domain/payment/PaymentStatus.java javadoc), not SIN_PAGOS/PARCIAL/SALDADO.
export type EstadoPago = "NoPayments" | "PARTIAL" | "PAID";
export function usePagoEstado(idPresupuesto: number | null) {
  return useQuery({
    queryKey: [...pagosKeys.all, "estado", idPresupuesto],
    queryFn: () => apiGet<EstadoPago>(`/pagos/presupuesto/${idPresupuesto}/estado`),
    enabled: idPresupuesto !== null,
  });
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
