/**
 * Testimony movement use cases (#1834 / ADR-030).
 */
import type { TestimonyMovementGateway } from "./ports";
import type { ReenterInput, WithdrawInput } from "../domain/testimony-movement";

export function createTestimonyMovementUseCases(gateway: TestimonyMovementGateway) {
  return {
    enterRegistration: (idTestimony: number) => gateway.enterRegistration(idTestimony),
    registerInscription: (idTestimony: number) => gateway.registerInscription(idTestimony),
    withdraw: (input: WithdrawInput) => gateway.withdraw(input),
    reenter: (input: ReenterInput) => gateway.reenter(input),
  };
}

export type TestimonyMovementUseCases = ReturnType<typeof createTestimonyMovementUseCases>;
