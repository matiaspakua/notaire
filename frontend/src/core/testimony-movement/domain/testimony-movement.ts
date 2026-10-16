/**
 * Testimony movement domain (#1834 / ADR-030).
 */

export interface TestimonyMovement {
  idTestimonyMovement?: number;
  dateEntry?: string;
  dateExit?: string;
  dateRegistration?: string;
  registered?: boolean;
  cardNumber?: number;
  observedByRegistry?: boolean;
  notes?: string;
  testimony?: { idTestimony?: number };
}

export interface WithdrawInput {
  idTestimony: number;
  cardNumber: number;
}

export interface ReenterInput {
  idTestimony: number;
  cardNumber: number;
  observedByRegistry: boolean;
  notes: string;
}
