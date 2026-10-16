// @vitest-environment node
/**
 * Payment use cases delegate to the gateway port (#1832).
 */
import { describe, expect, it } from "vitest";
import type { Payment, PaymentStatus } from "../domain/payment";
import type { PaymentGateway } from "./ports";
import { createPaymentUseCases } from "./use-cases";

function fakeGateway(initial: Payment[] = []): PaymentGateway & { store: Payment[] } {
  const store = [...initial];
  return {
    store,
    list: async () => [...store],
    listByBudget: async (idBudget) => store.filter((p) => p.idBudget === idBudget),
    listByDateRange: async (from, to) =>
      store.filter((p) => !!p.date && p.date >= from && p.date <= to),
    getStatus: async (idBudget): Promise<PaymentStatus> => {
      const rows = store.filter((p) => p.idBudget === idBudget);
      if (rows.length === 0) return "NoPayments";
      return "PARTIAL";
    },
    getBalance: async () => 42,
    create: async (input) => {
      store.push({ ...input, idPayment: store.length + 1 });
    },
    update: async (id, input) => {
      const idx = store.findIndex((p) => p.idPayment === id);
      if (idx >= 0) store[idx] = { ...store[idx], ...input };
    },
    delete: async (id) => {
      const idx = store.findIndex((p) => p.idPayment === id);
      if (idx >= 0) store.splice(idx, 1);
    },
  };
}

describe("payment use cases (#1832)", () => {
  it("lists and filters by budget and date", async () => {
    const gateway = fakeGateway([
      { idPayment: 1, idBudget: 10, amount: 100, date: "2026-01-01" },
      { idPayment: 2, idBudget: 20, amount: 200, date: "2026-02-01" },
    ]);
    const uc = createPaymentUseCases(gateway);
    await expect(uc.list()).resolves.toHaveLength(2);
    await expect(uc.listByBudget(10)).resolves.toEqual([
      expect.objectContaining({ idPayment: 1 }),
    ]);
    await expect(uc.listByDateRange("2026-01-15", "2026-02-15")).resolves.toEqual([
      expect.objectContaining({ idPayment: 2 }),
    ]);
  });

  it("creates with idBudget, reads status/balance, updates and deletes", async () => {
    const gateway = fakeGateway([]);
    const uc = createPaymentUseCases(gateway);

    await uc.create({ idBudget: 7, amount: 50, paymentMethod: "EFECTIVO" });
    expect(gateway.store[0]).toMatchObject({ idBudget: 7, amount: 50 });

    await expect(uc.getStatus(7)).resolves.toBe("PARTIAL");
    await expect(uc.getBalance(7)).resolves.toBe(42);

    await uc.update(1, { notes: "ok" });
    expect(gateway.store[0].notes).toBe("ok");

    await uc.delete(1);
    await expect(uc.list()).resolves.toHaveLength(0);
  });
});
