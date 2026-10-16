/**
 * Payment domain model (#1832 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 * Field names align with Payment / PaymentRequest wire shapes.
 */

export interface PaymentBudgetRef {
  idBudget?: number;
}

export interface Payment {
  idPayment?: number;
  /** Required on create (PaymentRequest.idBudget). */
  idBudget?: number;
  amount?: number;
  date?: string;
  paymentMethod?: string;
  notes?: string;
  fkIdBudget?: PaymentBudgetRef;
}

/**
 * Values match PaymentStatus.java verbatim — published REST contract
 * (not SIN_PAGOS/PARCIAL/SALDADO).
 */
export type PaymentStatus = "NoPayments" | "PARTIAL" | "PAID";
