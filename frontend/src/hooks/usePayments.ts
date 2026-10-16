/**
 * React Query hooks for payments.
 * Hexagon wiring: #1832 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { paymentUseCases } from "@/composition/payment";
import type { Payment as DomainPayment, PaymentStatus as DomainStatus } from "@/core/payment/domain/payment";
import type { Payment } from "@/types";

export const paymentsKeys = { all: ["payments"] as const };

// Values match PaymentStatus.java verbatim — its constant names are the published REST
// contract (see domain/payment/PaymentStatus.java javadoc), not SIN_PAGOS/PARCIAL/SALDADO.
export type PaymentStatus = DomainStatus;

function toWire(p: DomainPayment): Payment {
  return {
    idPayment: p.idPayment,
    idBudget: p.idBudget,
    amount: p.amount,
    date: p.date,
    paymentMethod: p.paymentMethod,
    notes: p.notes,
    fkIdBudget: p.fkIdBudget,
  };
}

export function usePayments() {
  return useQuery({
    queryKey: paymentsKeys.all,
    queryFn: async () => (await paymentUseCases.list()).map(toWire),
  });
}

export function usePaymentStatus(idBudget: number | null) {
  return useQuery({
    queryKey: [...paymentsKeys.all, "status", idBudget],
    queryFn: () => paymentUseCases.getStatus(idBudget!),
    enabled: idBudget !== null,
  });
}

/** CU47 (#1544): one budget's payments. */
export function usePaymentsByBudget(idBudget: number | null) {
  return useQuery({
    queryKey: [...paymentsKeys.all, "budget", idBudget],
    queryFn: async () => (await paymentUseCases.listByBudget(idBudget!)).map(toWire),
    enabled: idBudget !== null,
  });
}

/** CU47 (#1557): payments between two days, both inclusive (yyyy-MM-dd). */
export function usePaymentsByDate(from: string, to: string) {
  return useQuery({
    queryKey: [...paymentsKeys.all, "date", from, to],
    queryFn: async () => (await paymentUseCases.listByDateRange(from, to)).map(toWire),
    enabled: !!from && !!to,
  });
}

/** CU47 (#1544): a budget's outstanding balance. */
export function useBudgetBalance(idBudget: number | null) {
  return useQuery({
    queryKey: [...paymentsKeys.all, "balance", idBudget],
    queryFn: () => paymentUseCases.getBalance(idBudget!),
    enabled: idBudget !== null,
  });
}

export function useCreatePayment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (d: Partial<Payment>) => paymentUseCases.create(d),
    onSuccess: () => qc.invalidateQueries({ queryKey: paymentsKeys.all }),
  });
}

export function useUpdatePayment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Payment> }) =>
      paymentUseCases.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: paymentsKeys.all }),
  });
}

export function useDeletePayment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => paymentUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: paymentsKeys.all }),
  });
}
