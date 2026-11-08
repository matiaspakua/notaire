/**
 * Outbound port for payments (#1832 / ADR-030).
 */
import type { Payment, PaymentStatus } from "../domain/payment";

export type PaymentInput = Partial<Payment>;

export interface PaymentGateway {
  list(): Promise<Payment[]>;
  listByBudget(idBudget: number): Promise<Payment[]>;
  listByDateRange(from: string, to: string): Promise<Payment[]>;
  getStatus(idBudget: number): Promise<PaymentStatus>;
  getBalance(idBudget: number): Promise<number>;
  create(input: PaymentInput): Promise<void>;
  update(id: number, input: PaymentInput): Promise<void>;
  delete(id: number): Promise<void>;
}
