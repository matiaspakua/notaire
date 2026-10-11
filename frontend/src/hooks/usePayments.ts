import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Payment } from "@/types";

export const paymentsKeys = { all: ["payments"] as const };

export function usePayments() {
  return useQuery({ queryKey: paymentsKeys.all, queryFn: () => apiGet<Payment[]>("/payments") });
}
// Values match PaymentStatus.java verbatim — its constant names are the published REST
// contract (see domain/payment/PaymentStatus.java javadoc), not SIN_PAGOS/PARCIAL/SALDADO.
export type PaymentStatus = "NoPayments" | "PARTIAL" | "PAID";
export function usePaymentStatus(idBudget: number | null) {
  return useQuery({
    queryKey: [...paymentsKeys.all, "status", idBudget],
    queryFn: () => apiGet<PaymentStatus>(`/payments/budget/${idBudget}/status`),
    enabled: idBudget !== null,
  });
}
export function useCreatePayment() {
  const qc = useQueryClient();
  return useMutation({ mutationFn: (d: Partial<Payment>) => apiPost<void>("/payments", d), onSuccess: () => qc.invalidateQueries({ queryKey: paymentsKeys.all }) });
}
export function useUpdatePayment() {
  const qc = useQueryClient();
  return useMutation({ mutationFn: ({ id, data }: { id: number; data: Partial<Payment> }) => apiPut<void>(`/payments/${id}`, data), onSuccess: () => qc.invalidateQueries({ queryKey: paymentsKeys.all }) });
}
export function useDeletePayment() {
  const qc = useQueryClient();
  return useMutation({ mutationFn: (id: number) => apiDelete(`/payments/${id}`), onSuccess: () => qc.invalidateQueries({ queryKey: paymentsKeys.all }) });
}
