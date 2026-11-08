/**
 * Payment list/filter/mutation use cases (#1832 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { PaymentGateway, PaymentInput } from "./ports";

export function createPaymentUseCases(gateway: PaymentGateway) {
  return {
    list: () => gateway.list(),
    listByBudget: (idBudget: number) => gateway.listByBudget(idBudget),
    listByDateRange: (from: string, to: string) => gateway.listByDateRange(from, to),
    getStatus: (idBudget: number) => gateway.getStatus(idBudget),
    getBalance: (idBudget: number) => gateway.getBalance(idBudget),
    create: (input: PaymentInput) => gateway.create(input),
    update: (id: number, input: PaymentInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type PaymentUseCases = ReturnType<typeof createPaymentUseCases>;
